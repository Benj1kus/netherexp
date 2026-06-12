package com.benji.netherman.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

public class GuardianBehaviorGoal extends Goal {
    private final GuardianEntity guardian;
    private int tickCounter = 0;
    private int specCooldown = 0;
    private int punchCooldown = 0;
    private int comboPunchesLeft = 0; 
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
        if (target != null) {
            guardian.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (specCooldown > 0) specCooldown--;
        if (currentState == GuardianEntity.STATE_ANGRY || currentState == GuardianEntity.STATE_ATTACK_SPEC || currentState == GuardianEntity.STATE_ATTACK_CHANGE) {
            guardian.getNavigation().stop();
            if (tickCounter > 0) {
                tickCounter--;
                if (currentState == GuardianEntity.STATE_ATTACK_SPEC && tickCounter == 30) {
                    guardian.performMegaPunch();
                }
                if (tickCounter == 0) {
                    guardian.setEntityState(GuardianEntity.STATE_WALK);
                    pathUpdateDelay = 0;
                }
            }
            return;
        }
        if (currentState == GuardianEntity.STATE_MELEE) {
            guardian.getNavigation().stop();
            if (comboPunchesLeft > 0) {
                if (punchCooldown > 0) {
                    punchCooldown--;
                    if (punchCooldown == 10 || punchCooldown == 6 || punchCooldown == 2) {
                        guardian.performMeleeAttack();
                    }
                }
                if (punchCooldown == 0) {
                    comboPunchesLeft--;
                    if (comboPunchesLeft > 0) {
                        punchCooldown = 11;
                    } else {
                        guardian.setEntityState(GuardianEntity.STATE_ATTACK_CHANGE);
                        tickCounter = 15;
                    }
                }
            }
            return;
        }
        if (target == null) return;
        double distSq = guardian.distanceToSqr(target);
        if (currentState == GuardianEntity.STATE_WALK) {
            if (pathUpdateDelay <= 0) {
                guardian.getNavigation().moveTo(target, 1.0);
                pathUpdateDelay = 10;
            } else {
                pathUpdateDelay--;
            }
            if (distSq <= 16.0) {
                guardian.setEntityState(GuardianEntity.STATE_MELEE);
                comboPunchesLeft = guardian.getRandom().nextInt(3) + 3;
                punchCooldown = 11;
                guardian.getNavigation().stop();
            }
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
        if (state != GuardianEntity.STATE_ANGRY && state != GuardianEntity.STATE_MELEE &&
                state != GuardianEntity.STATE_ATTACK_SPEC && state != GuardianEntity.STATE_ATTACK_CHANGE) {
            guardian.setEntityState(GuardianEntity.STATE_NEUTRAL);
        }
        guardian.getNavigation().stop();
    }
}