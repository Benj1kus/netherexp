package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class AzazelHumanPhase2Goal extends Goal {
    private final AzazelHumanEntity boss;
    private int tick = 0;
    private final int MAX_TICKS = 180;

    public AzazelHumanPhase2Goal(AzazelHumanEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return boss.forcedAttackGoal == 5 && boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE) == 5;
    }

    @Override
    public void start() {
        boss.forcedAttackGoal = 0;
        boss.getNavigation().stop();
        tick = 0;
        boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 70);
        boss.level().playSound(null, boss.blockPosition(), ModSounds.ROAR.get(), SoundSource.HOSTILE, 4.0F, 1.0F);
    }

    @Override
    public void tick() {
        tick++;

        if (!boss.level().isClientSide() && tick < 150) {
            ServerLevel sl = (ServerLevel) boss.level();
            double radius = 8.0D;

            for (int i = 0; i < 15; i++) {
                double angle = boss.getRandom().nextDouble() * 2 * Math.PI;
                double x = boss.getX() + Math.cos(angle) * radius;
                double z = boss.getZ() + Math.sin(angle) * radius;

                double speedY = 0.5D + boss.getRandom().nextDouble() * 0.3D;
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, boss.getY() + 0.1, z, 0, 0.0, speedY, 0.0, 1.0);
            }
        }

        if (tick == 60) {
            boss.getEntityData().set(AzazelHumanEntity.IS_PHASE_2, true);
        }

        if (tick >= MAX_TICKS) {
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
        }
    }

    @Override
    public boolean canContinueToUse() {
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        return tick < MAX_TICKS && state < 100;
    }
}