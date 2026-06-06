package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;

import java.util.EnumSet;
import java.util.List;

public class ManipulatorConvertGoal extends Goal {
    private final ManipulatorEntity mob;
    private LivingEntity targetVictim;

    public ManipulatorConvertGoal(ManipulatorEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob.castTicks > 0) return false;

        // 1. Ищем обычных жителей в радиусе 10 блоков
        List<Villager> villagers = mob.level().getEntitiesOfClass(Villager.class, mob.getBoundingBox().inflate(10.0));
        if (!villagers.isEmpty()) {
            this.targetVictim = villagers.get(0);
            return true;
        }

        // 2. Если жителей нет, ищем Пиглинов
        List<Piglin> piglins = mob.level().getEntitiesOfClass(Piglin.class, mob.getBoundingBox().inflate(10.0));
        if (!piglins.isEmpty()) {
            this.targetVictim = piglins.get(0);
            return true;
        }

        // 3. --- НОВОЕ: Ищем Ghastly для убийства ---
        List<GhastlyEntity> ghastlies = mob.level().getEntitiesOfClass(GhastlyEntity.class, mob.getBoundingBox().inflate(10.0));
        if (!ghastlies.isEmpty()) {
            this.targetVictim = ghastlies.get(0);
            return true;
        }

        return false;
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
        mob.getLookControl().setLookAt(targetVictim);

        // Запускаем анимацию каста Манипулятора
        mob.setEntityState(ManipulatorEntity.STATE_ATTACK);
        mob.castTicks = 35;
    }

    @Override
    public void tick() {
        if (targetVictim != null) {
            mob.getLookControl().setLookAt(targetVictim);
        }

        // Когда каст завершается (остался 1 тик)
        if (mob.castTicks == 1 && targetVictim != null && targetVictim.isAlive()) {

            // Проверяем, кого именно мы поймали
            if (targetVictim instanceof Villager) {
                VillagerPrisonerEntity prisoner = NetherExp.VILLAGER_PRISONER.get().create(mob.level());
                if (prisoner != null) {
                    prisoner.moveTo(targetVictim.getX(), targetVictim.getY(), targetVictim.getZ(), targetVictim.getYRot(), 0);
                    prisoner.setMasterId(mob.getId());
                    mob.level().addFreshEntity(prisoner);
                }
                mob.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.0F, 1.0F);

            } else if (targetVictim instanceof Piglin) {
                PiglinPrisonerEntity prisoner = NetherExp.PIGLIN_PRISONER.get().create(mob.level());
                if (prisoner != null) {
                    prisoner.moveTo(targetVictim.getX(), targetVictim.getY(), targetVictim.getZ(), targetVictim.getYRot(), 0);
                    prisoner.setMasterId(mob.getId());
                    mob.level().addFreshEntity(prisoner);
                }
                mob.playSound(SoundEvents.PIGLIN_BRUTE_CONVERTED_TO_ZOMBIFIED, 1.0F, 0.8F);

            } else if (targetVictim instanceof GhastlyEntity ghastly) {
                // --- НОВОЕ: Казнь Ghastly ---
                ghastly.setHealth(0.0F);
                ghastly.die(mob.damageSources().magic());
                mob.playSound(SoundEvents.WITHER_SPAWN, 0.5F, 1.5F); // Жуткий звук при убийстве

                if (mob.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL, targetVictim.getX(), targetVictim.getY() + 0.5, targetVictim.getZ(), 30, 0.3, 0.3, 0.3, 0.1);
                }
            }

            // Общие эффекты превращения/убийства (красный дым)
            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(DustParticleOptions.REDSTONE, targetVictim.getX(), targetVictim.getY() + 1.0, targetVictim.getZ(), 30, 0.4, 0.8, 0.4, 0.1);
            }

            // Ghastly удаляется сам через метод die(), остальных удаляем принудительно
            if (!(targetVictim instanceof GhastlyEntity)) {
                targetVictim.discard();
            }
            targetVictim = null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return mob.castTicks > 0 && targetVictim != null && targetVictim.isAlive();
    }
}