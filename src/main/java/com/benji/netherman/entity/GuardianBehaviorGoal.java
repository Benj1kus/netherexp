package com.benji.netherman.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class GuardianBehaviorGoal extends Goal {
    private final GuardianEntity guardian;
    private int tickCounter = 0;
    private int specCooldown = 0;
    private int punchCooldown = 0;
    private int comboPunchesLeft = 0; // Счетчик ударов в серии
    private int pathUpdateDelay = 0;

    public GuardianBehaviorGoal(GuardianEntity guardian) {
        this.guardian = guardian;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (guardian.getEntityState() == GuardianEntity.STATE_SPAWNING) {
            return false;
        }
        if (guardian.getEntityState() == GuardianEntity.STATE_GREETING) {
            return false;
        }
        return guardian.getTarget() != null;
    }

    // Запрещаем ИИ прерывать ВСЕ атакующие анимации
    @Override
    public boolean canContinueToUse() {
        int state = guardian.getEntityState();
        if (state == GuardianEntity.STATE_ANGRY || state == GuardianEntity.STATE_MELEE ||
                state == GuardianEntity.STATE_ATTACK_SPEC || state == GuardianEntity.STATE_ATTACK_CHANGE) {
            return true;
        }
        return super.canContinueToUse();
    }

    @Override
    public void start() {
        guardian.setEntityState(GuardianEntity.STATE_ANGRY);
        tickCounter = 15;
        pathUpdateDelay = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = guardian.getTarget();
        int currentState = guardian.getEntityState();

        // Моб ВСЕГДА провожает игрока взглядом
        if (target != null) {
            guardian.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (specCooldown > 0) specCooldown--;

        // 1. ОДИНАРНЫЕ ЖЕСТКИЕ АНИМАЦИИ (Рык, Спец-атака, Завершение комбо)
        if (currentState == GuardianEntity.STATE_ANGRY || currentState == GuardianEntity.STATE_ATTACK_SPEC || currentState == GuardianEntity.STATE_ATTACK_CHANGE) {
            guardian.getNavigation().stop();
            if (tickCounter > 0) {
                tickCounter--;

                // Спец-атака на 0.83 сек (40 - 17 = 23) (Ты ставил 30, оставляю твой визуал!)
                if (currentState == GuardianEntity.STATE_ATTACK_SPEC && tickCounter == 30) {
                    guardian.performMegaPunch();
                }

                // Когда анимация завершилась — возвращаемся к преследованию
                if (tickCounter == 0) {
                    guardian.setEntityState(GuardianEntity.STATE_WALK);
                    pathUpdateDelay = 0;
                }
            }
            return; // Блокируем остальной код
        }

        // 2. НЕПРЕРЫВНОЕ КОМБО БЛИЖНЕГО БОЯ (STATE_MELEE)
        if (currentState == GuardianEntity.STATE_MELEE) {
            guardian.getNavigation().stop();

            // Мы полностью убрали фазу tickCounter (подготовки)!
            if (comboPunchesLeft > 0) {
                // Фаза лупа (attack_loop) длится 11 тиков
                if (punchCooldown > 0) {
                    punchCooldown--;

                    // Наносим урон 3 раза за один цикл (на 10, 6 и 2 тике)
                    // 10 означает, что первый удар пройдет почти моментально после начала анимации
                    if (punchCooldown == 10 || punchCooldown == 6 || punchCooldown == 2) {
                        guardian.performMeleeAttack();
                    }
                }

                // Один луп из серии завершен
                if (punchCooldown == 0) {
                    comboPunchesLeft--;
                    if (comboPunchesLeft > 0) {
                        punchCooldown = 11; // Запускаем следующий взмах
                    } else {
                        // Комбо закончилось! Запускаем анимацию отмены (0.75 сек)
                        guardian.setEntityState(GuardianEntity.STATE_ATTACK_CHANGE);
                        tickCounter = 15;
                    }
                }
            }
            return; // Блокируем остальной код, пока комбо не закончится
        }

        // Если мы не в фазе атаки и цель пропала — выходим
        if (target == null) return;
        double distSq = guardian.distanceToSqr(target);

        // 3. ЛОГИКА ПРЕСЛЕДОВАНИЯ (STATE_WALK)
        if (currentState == GuardianEntity.STATE_WALK) {
            if (pathUpdateDelay <= 0) {
                guardian.getNavigation().moveTo(target, 1.0);
                pathUpdateDelay = 10;
            } else {
                pathUpdateDelay--;
            }

            // Начинаем серию ближнего боя, если игрок в радиусе 4 блоков (16.0)
            if (distSq <= 16.0) {
                guardian.setEntityState(GuardianEntity.STATE_MELEE);
                // СРАЗУ заряжаем комбо и таймер, без подготовки!
                comboPunchesLeft = guardian.getRandom().nextInt(3) + 3;
                punchCooldown = 11;
                guardian.getNavigation().stop();
            }
            // Или запускаем спец-атаку (10 блоков)
            else if (distSq <= 100.0 && specCooldown == 0 && guardian.getRandom().nextInt(20) == 0) {
                guardian.setEntityState(GuardianEntity.STATE_ATTACK_SPEC);
                tickCounter = 40;
                specCooldown = 160;
                guardian.getNavigation().stop();
            }
        }
    }

    @Override
    public void stop() {
        int state = guardian.getEntityState();
        // Сбрасываем стейт только если моб не в защищенной анимации
        if (state != GuardianEntity.STATE_ANGRY && state != GuardianEntity.STATE_MELEE &&
                state != GuardianEntity.STATE_ATTACK_SPEC && state != GuardianEntity.STATE_ATTACK_CHANGE) {
            guardian.setEntityState(GuardianEntity.STATE_NEUTRAL);
        }
        guardian.getNavigation().stop();
    }
}