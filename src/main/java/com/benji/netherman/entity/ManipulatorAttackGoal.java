package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import java.util.EnumSet;

public class ManipulatorAttackGoal extends Goal {
    private final ManipulatorEntity mob;
    private int attackCooldown = 40;

    public ManipulatorAttackGoal(ManipulatorEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        double distSq = mob.distanceToSqr(target);
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 1. Механика бегства (скорость х2 при получении урона)
        if (mob.fleeTicks > 0) {
            mob.getNavigation().moveTo(mob.getX() + (mob.getX() - target.getX()), mob.getY(), mob.getZ() + (mob.getZ() - target.getZ()), 2.0D); // Убегает от цели
            return;
        }

        // Блокируем движение во время каста
        if (mob.castTicks > 0) {
            mob.getNavigation().stop();
            return;
        }

        if (attackCooldown > 0) attackCooldown--;

        // 2. ИИ ПОДДЕРЖАНИЯ ДИСТАНЦИИ (8 - 10 блоков)
        if (distSq < 64.0D) {
            // Игрок ближе 8 блоков - ПЯТИМСЯ НАЗАД
            mob.getNavigation().moveTo(mob.getX() + (mob.getX() - target.getX()), mob.getY(), mob.getZ() + (mob.getZ() - target.getZ()), 1.0D);
        } else if (distSq > 100.0D) {
            // Игрок дальше 10 блоков - ИДЕМ К НЕМУ
            mob.getNavigation().moveTo(target, 1.0D);
        } else {
            // Идеальная дистанция - стоим
            mob.getNavigation().stop();
        }

        // Смена стейта ходьбы
        if (mob.getNavigation().isInProgress()) {
            if (mob.getEntityState() != ManipulatorEntity.STATE_RUN) mob.setEntityState(ManipulatorEntity.STATE_WALK);
        } else {
            if (mob.getEntityState() != ManipulatorEntity.STATE_RUN) mob.setEntityState(ManipulatorEntity.STATE_IDLE);
        }

        // Атака дальнего боя (в радиусе 15 блоков -> 15*15 = 225)
        if (distSq <= 225.0D && attackCooldown == 0) {
            mob.getNavigation().stop();

            // Проверяем кулдаун эффекта (1200 тиков = 1 минута)
            if (mob.manipulationCooldown <= 0 && target instanceof Player player) {
                // Манипуляция разумом: сначала анимация attack (35 тиков), затем attack_loop
                mob.setEntityState(ManipulatorEntity.STATE_ATTACK);
                mob.castTicks = 75; // Общее время каста (1.75 + 2 сек)

                // УСТАНАВЛИВАЕМ КУЛДАУН НА 1 МИНУТУ
                mob.manipulationCooldown = 1200;

                // Накладываем эффект на 2 минуты (2400 тиков)
                player.addEffect(new MobEffectInstance(NetherExp.MANIPULATION_EFFECT.get(), 200, 0));

                // Переключаем в луп через 1.75 сек с помощью отложенного стейта
                mob.level().getServer().tell(new net.minecraft.server.TickTask(mob.level().getServer().getTickCount() + 35, () -> {
                    if (mob.isAlive() && mob.castTicks > 0) mob.setEntityState(ManipulatorEntity.STATE_ATTACK_LOOP);
                }));

            } else {
                // Призыв Скелетов Иссушителей (кастуем, пока магия разума в откате)
                mob.setEntityState(ManipulatorEntity.STATE_ATTACK);
                mob.castTicks = 35; // Только первая часть анимации
                mob.spawnWitherSkeletons();
            }

            attackCooldown = 160; // Общий кулдаун между любыми атаками (8 секунд)
        }
    }
}