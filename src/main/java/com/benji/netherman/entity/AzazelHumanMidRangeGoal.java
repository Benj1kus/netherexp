package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.config.AzazelConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AzazelHumanMidRangeGoal extends Goal {
    private final AzazelHumanEntity boss;
    private int attackState = 0; 
    private int currentAnimTick = 0;
    private int maxAnimTick = 0;

    public AzazelHumanMidRangeGoal(AzazelHumanEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        if (state != 5) return false;

        if (boss.forcedAttackGoal == 2) return true;

        LivingEntity target = boss.getTarget();
        if (target == null) return false;

        double distSq = boss.distanceToSqr(target);

        double minRadius = AzazelConfig.HUMAN_MELEE_ATTACK_RADIUS.get();
        double maxRadius = AzazelConfig.HUMAN_MID_ATTACK_RADIUS_MAX.get();

        return distSq > (minRadius * minRadius) && distSq <= (maxRadius * maxRadius);
    }

    @Override
    public void start() {
        boss.getNavigation().stop();
        currentAnimTick = 0;
        boss.forcedAttackGoal = 0;

        LivingEntity target = boss.getTarget();
        if (target != null) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        int rand = boss.getRandom().nextInt(100);

        if (rand < 40) {
            attackState = 30; 
            maxAnimTick = 24; 
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 30);
            playRandomGrunt(); 
        } else if (rand < 80) {
            attackState = 31; 
            maxAnimTick = 40; 
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 31);
            playRandomGrunt(); 
        } else {
            attackState = 32; 
            maxAnimTick = 120; 
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 32);
        }
    }

    @Override
    public void tick() {
        currentAnimTick++;
        LivingEntity target = boss.getTarget();

        
        if (target != null && currentAnimTick < (maxAnimTick / 3)) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (attackState == 30) {
            
            
            if (currentAnimTick == 8) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.SWING_1.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                executeSpearAttack(AzazelConfig.HUMAN_SPEAR_MID_PUSH_DAMAGE.get().floatValue(), AzazelConfig.HUMAN_SPEAR_MID_PUSH_KNOCKBACK.get(), false);
            }
        }
        else if (attackState == 31) {
            
            
            if (currentAnimTick == 10) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.SWING_2.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                executeSpearAttack(AzazelConfig.HUMAN_SPEAR_MID_PULL_DAMAGE.get().floatValue(), AzazelConfig.HUMAN_SPEAR_MID_PULL_KNOCKBACK.get(), true);
            }
        }

        else if (attackState == 32) {
            
            if (currentAnimTick == 15) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.SMOKE_BREATH.get(), SoundSource.HOSTILE, 3.0F, 1.0F);
            }

            if (currentAnimTick >= 20 && currentAnimTick <= 80 && currentAnimTick % 2 == 0) {
                if (boss.level() instanceof ServerLevel sl) {
                    Vec3 look = boss.getLookAngle();
                    
                    double spawnX = boss.getX() + look.x * 3.0D;
                    double spawnZ = boss.getZ() + look.z * 3.0D;

                    
                    double spawnY = boss.getY() + 7.5D;

                    for (int i = 0; i < 15; i++) {
                        double offsetX = (boss.getRandom().nextDouble() - 0.5D) * 6.0D;
                        double offsetZ = (boss.getRandom().nextDouble() - 0.5D) * 6.0D;

                        sl.sendParticles(ParticleTypes.LARGE_SMOKE,
                                spawnX + offsetX, spawnY, spawnZ + offsetZ,
                                0, 0.0D, -0.5D, 0.0D, 0.5D); 
                    }
                }
            }

            if (currentAnimTick == 40) {
                for (Player p : boss.level().getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(20.0D))) {
                    p.addEffect(new MobEffectInstance(MobEffects.WITHER, 600, 1));
                    p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 600, 0));
                    p.addEffect(new MobEffectInstance(NetherExp.MANIPULATION_EFFECT.get(), 400, 0));
                }
            }
        }

        
        if (currentAnimTick >= maxAnimTick) {
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
            attackState = 0;
        }
    }

    private void executeSpearAttack(float damage, double force, boolean pull) {
        Vec3 look = boss.getLookAngle();
        Vec3 look2D = new Vec3(look.x, 0, look.z).normalize();

        
        for (Player p : boss.level().getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(12.0D))) {
            Vec3 toPlayer = p.position().subtract(boss.position());
            Vec3 toPlayer2D = new Vec3(toPlayer.x, 0, toPlayer.z).normalize();

            
            if (look2D.dot(toPlayer2D) > 0.0D) {
                p.hurt(boss.damageSources().mobAttack(boss), damage);
                p.hurtMarked = true;

                if (pull) {
                    
                    p.setDeltaMovement(p.getDeltaMovement().add(-toPlayer2D.x * force, 0.3D, -toPlayer2D.z * force));
                } else {
                    
                    p.setDeltaMovement(p.getDeltaMovement().add(toPlayer2D.x * force, 0.5D, toPlayer2D.z * force));
                }
            }
        }
    }

    private void playRandomGrunt() {
        SoundEvent[] grunts = {
                ModSounds.GRUNT_1.get(), ModSounds.GRUNT_3.get(), ModSounds.GRUNT_4.get(),
                ModSounds.GRUNT_5.get(), ModSounds.GRUNT_6.get(), ModSounds.GRUNT_7.get()
        };
        boss.level().playSound(null, boss.blockPosition(), grunts[boss.getRandom().nextInt(grunts.length)], SoundSource.HOSTILE, 2.0F, 0.9F + boss.getRandom().nextFloat() * 0.2F);
    }

    @Override
    public boolean canContinueToUse() {
        
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        return currentAnimTick < maxAnimTick && state != 40 && state < 100;
    }
}