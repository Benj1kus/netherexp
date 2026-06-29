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
        if (mob.fleeTicks > 0) {
            mob.getNavigation().moveTo(mob.getX() + (mob.getX() - target.getX()), mob.getY(), mob.getZ() + (mob.getZ() - target.getZ()), 2.0D); 
            return;
        }
        if (mob.castTicks > 0) {
            mob.getNavigation().stop();
            return;
        }

        if (attackCooldown > 0) attackCooldown--;
        if (distSq < 64.0D) {
            mob.getNavigation().moveTo(mob.getX() + (mob.getX() - target.getX()), mob.getY(), mob.getZ() + (mob.getZ() - target.getZ()), 1.0D);
        } else if (distSq > 100.0D) {
            mob.getNavigation().moveTo(target, 1.0D);
        } else {
            mob.getNavigation().stop();
        }
        if (mob.getNavigation().isInProgress()) {
            if (mob.getEntityState() != ManipulatorEntity.STATE_RUN) mob.setEntityState(ManipulatorEntity.STATE_WALK);
        } else {
            if (mob.getEntityState() != ManipulatorEntity.STATE_RUN) mob.setEntityState(ManipulatorEntity.STATE_IDLE);
        }
        if (distSq <= 225.0D && attackCooldown == 0) {
            mob.getNavigation().stop();
            if (mob.manipulationCooldown <= 0 && target instanceof Player player) {
                mob.setEntityState(ManipulatorEntity.STATE_ATTACK);
                mob.castTicks = 75; 
                mob.manipulationCooldown = 1200;
                player.addEffect(new MobEffectInstance(NetherExp.MANIPULATION_EFFECT.get(), 200, 0));
                mob.level().getServer().tell(new net.minecraft.server.TickTask(mob.level().getServer().getTickCount() + 35, () -> {
                    if (mob.isAlive() && mob.castTicks > 0) mob.setEntityState(ManipulatorEntity.STATE_ATTACK_LOOP);
                }));

            } else {
                mob.setEntityState(ManipulatorEntity.STATE_ATTACK);
                mob.castTicks = 35;
                mob.spawnWitherSkeletons();
            }

            attackCooldown = 160;
        }
    }
}