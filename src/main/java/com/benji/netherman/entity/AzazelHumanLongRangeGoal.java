package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.config.AzazelConfig;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AzazelHumanLongRangeGoal extends Goal {
    private final AzazelHumanEntity boss;
    private int attackState = 0; 
    private int currentAnimTick = 0;
    private int maxAnimTick = 0;
    private int cooldown = 0;

    
    private int spikePattern = 0;
    private double spikeTargetX = 0;
    private double spikeTargetZ = 0;

    public AzazelHumanLongRangeGoal(AzazelHumanEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) cooldown--;

        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        if (state != 5) return false;

        if (boss.forcedAttackGoal == 3) return true;

        LivingEntity target = boss.getTarget();
        if (target == null) return false;

        double longRadiusMin = AzazelConfig.HUMAN_LONG_ATTACK_RADIUS_MIN.get();

        if (boss.distanceToSqr(target) > (longRadiusMin * longRadiusMin) && cooldown <= 0) {
            return boss.getRandom().nextBoolean();
        }
        return false;
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        currentAnimTick = 0;
        boss.forcedAttackGoal = 0;

        LivingEntity target = boss.getTarget();
        if (target != null) {
            double dX = target.getX() - boss.getX();
            double dZ = target.getZ() - boss.getZ();
            float yaw = (float) (Math.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
            boss.setYRot(yaw);
            boss.yBodyRot = yaw;
            boss.yHeadRot = yaw;
        }

        
        int rand = boss.getRandom().nextInt(100);

        if (rand < 25) {
            attackState = 50; 
            maxAnimTick = 60;
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 50);
        } else if (rand < 50) {
            attackState = 51; 
            maxAnimTick = 50;
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 51);
            spikePattern = boss.getRandom().nextInt(4) + 1;

            if (spikePattern == 1 || spikePattern == 2) {
                spikeTargetX = boss.getX();
                spikeTargetZ = boss.getZ();
            } else if (target != null) {
                spikeTargetX = target.getX();
                spikeTargetZ = target.getZ();
            }
        } else if (rand < 75) {
            attackState = 52; 
            maxAnimTick = 50;
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 52);
        } else {
            attackState = 53; 
            maxAnimTick = 50;
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 53);
        }

        cooldown = 100;
    }

    @Override
    public void tick() {
        currentAnimTick++;
        LivingEntity target = boss.getTarget();

        if (target != null && currentAnimTick < (maxAnimTick / 2)) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (attackState == 50) {
            
            if (currentAnimTick == 10 || currentAnimTick == 20 || currentAnimTick == 30) {
                SoundEvent swingSound = (currentAnimTick == 20) ? ModSounds.SWING_2.get() : ModSounds.SWING_1.get();
                boss.level().playSound(null, boss.blockPosition(), swingSound, SoundSource.HOSTILE, 2.0F, 0.8F + boss.getRandom().nextFloat() * 0.4F);

                if (!boss.level().isClientSide() && target != null) {
                    AzazelSplashEntity splash = NetherExp.SPLASH_ENTITY.get().create(boss.level());
                    if (splash != null) {
                        Vec3 look = boss.getLookAngle();
                        splash.setPos(boss.getX() + look.x * 2.0, boss.getY() + 3.0, boss.getZ() + look.z * 2.0);
                        splash.setOwner(boss);
                        double dX = target.getX() - splash.getX();
                        double dY = target.getY(0.5D) - splash.getY();
                        double dZ = target.getZ() - splash.getZ();
                        splash.shoot(dX, dY, dZ, 1.5F, 0.0F);
                        boss.level().addFreshEntity(splash);
                    }
                }
            }
        }
        else if (attackState == 51) {
            
            if (currentAnimTick == 10) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.DODGE.get(), SoundSource.HOSTILE, 2.0F, 0.8F);
                if (!boss.level().isClientSide()) spawnSpikePattern();
            }
            if (spikePattern == 4 && !boss.level().isClientSide()) {
                if (currentAnimTick == 30) spawnSquareBorder(8);
                if (currentAnimTick == 40) spawnSquareBorder(5);
                if (currentAnimTick == 50) spawnSquareBorder(3);
            }
        }
        else if (attackState == 52) {
            
            if (currentAnimTick == 10) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.DODGE.get(), SoundSource.HOSTILE, 2.0F, 0.8F);

                if (!boss.level().isClientSide()) {
                    
                    for (int angle = 0; angle < 360; angle += 90) {
                        
                        for (int r = 1; r <= 15; r++) {
                            spawnProjectileSpike(1, r, angle, 0, 0); 
                        }
                    }
                }
            }
        }
        else if (attackState == 53) {
            
            if (currentAnimTick == 10 || currentAnimTick == 15 || currentAnimTick == 20) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.SWING_1.get(), SoundSource.HOSTILE, 2.0F, 1.2F);

                if (!boss.level().isClientSide()) {
                    
                    for (int angle = 0; angle < 360; angle += 45) {
                        double rad = Math.toRadians(angle);
                        double dX = Math.cos(rad);
                        double dZ = Math.sin(rad);
                        spawnProjectileSpike(0, 0, 0, dX, dZ); 
                    }
                }
            }
        }

        if (currentAnimTick >= maxAnimTick) {
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
            attackState = 0;
        }
    }

    
    private void spawnProjectileSpike(int mode, float radius, float angle, double dX, double dZ) {
        
        AzazelSpikesProjectileEntity proj = NetherExp.SPIKE_PROJECTILE_ENTITY.get().create(boss.level());
        if (proj != null) {
            proj.setOwner(boss);
            proj.setMode(mode);

            if (mode == 1) {
                
                proj.setPivotId(boss.getId());
                proj.setOrbitRadius(radius);
                proj.setOrbitAngle(angle);
                proj.setPos(boss.getX(), boss.getY() + 0.5D, boss.getZ());
            } else {
                
                proj.setPos(boss.getX() + dX * 2.0, boss.getY() + 1.0D, boss.getZ() + dZ * 2.0);
                proj.shoot(dX, 0.0D, dZ, 0.8F, 0.0F); 
            }
            boss.level().addFreshEntity(proj);
        }
    }

    
    private void spawnSpikePattern() {
        if (spikePattern == 1) {
            
            for (int i = 1; i <= 15; i++) {
                spawnSingleSpike(spikeTargetX + i, spikeTargetZ); 
                spawnSingleSpike(spikeTargetX - i, spikeTargetZ); 
                spawnSingleSpike(spikeTargetX, spikeTargetZ + i); 
                spawnSingleSpike(spikeTargetX, spikeTargetZ - i); 
            }
        }
        else if (spikePattern == 2) {
            
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    spawnSingleSpike(spikeTargetX + x, spikeTargetZ + z);
                }
            }
        }
        else if (spikePattern == 3) {
            
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    spawnSingleSpike(spikeTargetX + x, spikeTargetZ + z);
                }
            }
        }
        else if (spikePattern == 4) {
            
            spawnSquareBorder(10);
        }
    }

    
    private void spawnSquareBorder(int size) {
        int half = size / 2;
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                
                if (Math.abs(x) == half || Math.abs(z) == half) {
                    spawnSingleSpike(spikeTargetX + x, spikeTargetZ + z);
                }
            }
        }
    }

    private void spawnSingleSpike(double x, double z) {
        AzazelSpikeEntity spike = NetherExp.SPIKE_ENTITY.get().create(boss.level());
        if (spike != null) {
            
            spike.setPos(x, boss.getY(), z);
            spike.setOwner(boss);
            boss.level().addFreshEntity(spike);
        }
    }

    @Override
    public boolean canContinueToUse() {
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        return currentAnimTick < maxAnimTick && state != 40 && state < 100;
    }
}