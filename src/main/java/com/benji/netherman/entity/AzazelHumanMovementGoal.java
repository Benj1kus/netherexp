package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.config.AzazelConfig;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AzazelHumanMovementGoal extends Goal {
    private final AzazelHumanEntity boss;

    
    private Vec3 chargeDirection = Vec3.ZERO;
    private int moveMode = 0; 
    private int actionTimer = 0;

    public AzazelHumanMovementGoal(AzazelHumanEntity boss) {
        this.boss = boss;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        if (state < 5 || state == 14) return false; 

        LivingEntity target = boss.getTarget();
        if (target == null) return false;

        double stopRadius = AzazelConfig.HUMAN_MOVEMENT_STOP_RADIUS.get();

        return boss.distanceToSqr(target) > (stopRadius * stopRadius);
    }

    
    private int pathUpdateTimer = 0;


    @Override
    public void start() {
        this.pathUpdateTimer = 0;

        boolean isPhase2 = boss.getEntityData().get(AzazelHumanEntity.IS_PHASE_2);
        int rand = boss.getRandom().nextInt(100);

        if (isPhase2) {
            if (rand < 10) {
                moveMode = 0; // Walk (10%)
                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 10);
            } else if (rand < 15) {
                moveMode = 1; // DefendWalk (5%)
                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 11);
            } else if (rand < 55) {
                moveMode = 2; // Charge (40%)
                startCharge();
            } else {
                moveMode = 3; // Jump (45%)
                startJump();
            }
        } else {
            if (rand < 40) {
                moveMode = 0;
                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 10);
            } else if (rand < 70) {
                moveMode = 1;
                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 11);
            } else if (rand < 90) {
                moveMode = 2;
                startCharge();
            } else {
                moveMode = 3;
                startJump();
            }
        }
    }

    private void startCharge() {
        boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 12);
        actionTimer = 60;
        LivingEntity target = boss.getTarget();
        if (target != null) {
            chargeDirection = target.position().subtract(boss.position()).normalize();
            boss.getLookControl().setLookAt(target.getX(), boss.getEyeY(), target.getZ());
        }
    }

    private void startJump() {
        boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 13);
        actionTimer = 43;
        LivingEntity target = boss.getTarget();
        if (target != null) {
            Vec3 jumpVec = target.position().subtract(boss.position()).normalize().scale(1.5D);
            boss.setDeltaMovement(jumpVec.x, 1.2D, jumpVec.z);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = boss.getTarget();
        if (target == null) return;

        if (moveMode == 0 || moveMode == 1) {
            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

            
            
            
            if (this.pathUpdateTimer <= 0 || boss.getNavigation().isDone()) {
                boss.getNavigation().moveTo(target, 1.0D);
                this.pathUpdateTimer = 20;
            } else {
                this.pathUpdateTimer--;
            }

            double stopRadius = AzazelConfig.HUMAN_MOVEMENT_STOP_RADIUS.get();
            if (boss.distanceToSqr(target) <= (stopRadius * stopRadius)) {
                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
            }
        }
        else if (moveMode == 2) {
            actionTimer--;
            boss.setDeltaMovement(chargeDirection.x * 0.8, boss.getDeltaMovement().y, chargeDirection.z * 0.8);

            float targetYaw = (float)(Math.atan2(chargeDirection.z, chargeDirection.x) * (180F / Math.PI)) - 90.0F;
            boss.setYRot(targetYaw);
            boss.yBodyRot = targetYaw;
            boss.yHeadRot = targetYaw;

            float chargeDamage = AzazelConfig.HUMAN_CHARGE_DAMAGE.get().floatValue();
            double chargeKb = AzazelConfig.HUMAN_CHARGE_KNOCKBACK.get();
            
            for (Player p : boss.level().getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(1.5D))) {

                p.setDeltaMovement(chargeDirection.x * chargeKb, 0.2D, chargeDirection.z * chargeKb);
                p.hurtMarked = true;

                
                if (actionTimer % 5 == 0) {
                    p.hurt(boss.damageSources().mobAttack(boss), 30.0F);
                }
            }

            net.minecraft.world.phys.AABB crashBox = boss.getBoundingBox().move(chargeDirection.x * 1.5, 0, chargeDirection.z * 1.5);
            boolean hitWall = boss.horizontalCollision || !boss.level().noCollision(boss, crashBox);

            if (hitWall) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.STOMP.get(), SoundSource.HOSTILE, 3.0F, 1.0F);
                boss.triggerScreenShake(2.0F, 30);

                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 14);

                boss.getEntityData().set(AzazelHumanEntity.ATTACK_TIMER, 90);
                boss.setDeltaMovement(Vec3.ZERO);
            }
            
            else if (actionTimer <= 0) {
                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
            }
        }
        else if (moveMode == 3) {
            actionTimer--;

            boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (actionTimer <= 0 || (boss.onGround() && actionTimer < 30)) {
                boss.level().playSound(null, boss.blockPosition(), ModSounds.STOMP.get(), SoundSource.HOSTILE, 3.0F, 0.8F);
                boss.triggerScreenShake(2.5F, 40);

                for (Player p : boss.level().getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(20.0D))) {
                    p.setDeltaMovement(p.getDeltaMovement().x, 1.5D, p.getDeltaMovement().z);
                    p.hurtMarked = true;
                }
                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        
        return state >= 10 && state <= 13;
    }

    @Override
    public void stop() {
        boss.getNavigation().stop();
        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        
        if (state != 14 && state != 40) {
            boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 5);
        }
    }
}