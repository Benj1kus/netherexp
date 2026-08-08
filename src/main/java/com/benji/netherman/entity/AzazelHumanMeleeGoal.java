package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.config.AzazelConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AzazelHumanMeleeGoal extends Goal {
    private final AzazelHumanEntity boss;
    private int attackState = 0; 

    private int currentAnimTick = 0; 
    private int maxAnimTick = 0;

    public AzazelHumanMeleeGoal(AzazelHumanEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        if (state != 5) return false;

        if (boss.forcedAttackGoal == 1) return true;

        LivingEntity target = boss.getTarget();
        if (target == null) return false;

        double meleeRadius = AzazelConfig.HUMAN_MELEE_ATTACK_RADIUS.get();
        double meleeRadiusSq = meleeRadius * meleeRadius;

        return boss.distanceToSqr(target) <= meleeRadiusSq || (boss.getDamageTakenRecently() >= 25.0F && boss.getDefendCooldown() <= 0);
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

        if (boss.getDamageTakenRecently() >= 25.0F && boss.getDefendCooldown() <= 0) {
            attackState = 22;
            maxAnimTick = 100; 
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 22);

            boss.heal(100.0F);
            boss.setDefendCooldown(2400);
            boss.resetDamageTaken();
            return;
        }

        int rand = boss.getRandom().nextInt(100);

        if (rand < 40) {
            attackState = 20; 
            maxAnimTick = 38;
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 20);
        } else if (rand < 80) {
            attackState = 21; 
            maxAnimTick = 60;
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 21);
            boss.level().playSound(null, boss.blockPosition(), ModSounds.GRUNT_2.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
        } else if (rand < 90) {
            attackState = 30; 
            maxAnimTick = 24;
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 30);
            playRandomGrunt();
        }
    }

    @Override
    public void tick() {
        currentAnimTick++;
        LivingEntity target = boss.getTarget();

        
        if (target != null && currentAnimTick < (maxAnimTick / 2)) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (attackState == 20) {

            if (currentAnimTick == 15) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.STOMP.get(), SoundSource.HOSTILE, 3.0F, 0.8F);
                boss.triggerScreenShake(5.0F, 30);

                if (boss.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, boss.getX(), boss.getY() + 0.5, boss.getZ(), 50, 2.0, 0.2, 2.0, 0.1);
                }

                float stompDamage = AzazelConfig.HUMAN_STOMP_DAMAGE.get().floatValue();
                double stompKb = AzazelConfig.HUMAN_STOMP_KNOCKBACK.get();

                for (Player p : boss.level().getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(15.0D))) {
                    Vec3 push = p.position().subtract(boss.position()).normalize();

                    p.setDeltaMovement(push.x * stompKb, stompKb * 1.5D, push.z * stompKb);
                    p.hurt(boss.damageSources().mobAttack(boss), stompDamage);
                    p.hurtMarked = true;
                }
            }
        }
        else if (attackState == 21) {
            
            
            if (target != null && currentAnimTick <= 10) {
                double dX = target.getX() - boss.getX();
                double dZ = target.getZ() - boss.getZ();
                float yaw = (float) (Mth.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
                boss.setYRot(yaw);
                boss.yBodyRot = yaw;
                boss.yHeadRot = yaw;
            }
            if (currentAnimTick == 5) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.SWING_1.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                hitWithScythe(AzazelConfig.HUMAN_SCYTHE_1_DAMAGE.get().floatValue(), AzazelConfig.HUMAN_SCYTHE_1_KNOCKBACK.get());
            }
            else if (currentAnimTick == 15) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.SWING_2.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                hitWithScythe(AzazelConfig.HUMAN_SCYTHE_2_DAMAGE.get().floatValue(), AzazelConfig.HUMAN_SCYTHE_2_KNOCKBACK.get());
            }
        }
        else if (attackState == 30) {
            if (currentAnimTick == 8) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.SWING_1.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
                executeSpearAttack(AzazelConfig.HUMAN_SPEAR_MELEE_DAMAGE.get().floatValue(), AzazelConfig.HUMAN_SPEAR_MELEE_KNOCKBACK.get(), false);
            }
        }
        else if (attackState == 22) {
            
            if (currentAnimTick % 10 == 0 && boss.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, boss.getX(), boss.getY() + 2.0, boss.getZ(), 10, 1.5, 1.5, 1.5, 0.1);
            }
        }

        
        if (currentAnimTick >= maxAnimTick) {
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
            attackState = 0;
        }
    }

    private void hitWithScythe(float damage, double knockbackMultiplier) {
        Vec3 look = boss.getLookAngle();
        Vec3 look2D = new Vec3(look.x, 0, look.z).normalize();

        
        for (Player p : boss.level().getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(12.0D))) {
            Vec3 toPlayer = p.position().subtract(boss.position());
            Vec3 toPlayer2D = new Vec3(toPlayer.x, 0, toPlayer.z).normalize();

            
            if (toPlayer2D.lengthSqr() < 0.01D) {
                p.hurt(boss.damageSources().mobAttack(boss), damage);
                p.setDeltaMovement(0, 0.4D, 0); 
                p.hurtMarked = true;
                continue;
            }

            if (look2D.dot(toPlayer2D) > -0.2D) {
                p.hurt(boss.damageSources().mobAttack(boss), damage);
                p.setDeltaMovement(p.getDeltaMovement().add(toPlayer2D.x * knockbackMultiplier, 0.4D, toPlayer2D.z * knockbackMultiplier));
                p.hurtMarked = true;
            }
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