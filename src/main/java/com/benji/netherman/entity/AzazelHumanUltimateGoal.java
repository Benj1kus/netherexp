package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class AzazelHumanUltimateGoal extends Goal {
    private final AzazelHumanEntity boss;
    private int currentAnimTick = 0;
    private final int maxAnimTick = 38;

    public AzazelHumanUltimateGoal(AzazelHumanEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return boss.forcedAttackGoal == 4 && boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE) == 5;
    }

    @Override
    public void start() {
        boss.forcedAttackGoal = 0;
        boss.getNavigation().stop();
        currentAnimTick = 0;
        boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 60);
    }

    @Override
    public void tick() {
        currentAnimTick++;

        if (currentAnimTick == 15) {
            boss.level().playSound(null, boss.blockPosition(), ModSounds.STOMP.get(), SoundSource.HOSTILE, 3.0F, 0.8F);
            boss.level().playSound(null, boss.blockPosition(), ModSounds.GRUNT_8.get(), SoundSource.HOSTILE, 2.5F, 1.0F);
            boss.triggerScreenShake(8.0F, 50);

            if (!boss.level().isClientSide()) {
                AzazelEarthquakeEntity quake = NetherExp.EARTHQUAKE_ENTITY.get().create(boss.level());
                if (quake != null) {
                    quake.setPos(boss.getX(), boss.getY(), boss.getZ());
                    boss.level().addFreshEntity(quake);
                }
            }
        }

        if (currentAnimTick >= maxAnimTick) {
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
        }
    }

    @Override
    public boolean canContinueToUse() {
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        return currentAnimTick < maxAnimTick && state != 40 && state < 100;
    }
}