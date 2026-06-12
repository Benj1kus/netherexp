package com.benji.netherman.entity;

public class GhastlyEnterHiveGoal extends net.minecraft.world.entity.ai.goal.Goal {
    private final GhastlyEntity ghastly;
    private net.minecraft.core.BlockPos targetHive = null;

    public GhastlyEnterHiveGoal(GhastlyEntity ghastly) {
        this.ghastly = ghastly;
        this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (ghastly.isTame() || ghastly.eatTicks > 0) return false;

        if (ghastly.getRandom().nextInt(40) == 0) {
            net.minecraft.core.BlockPos mobPos = ghastly.blockPosition();
            for (int x = -10; x <= 10; x++) {
                for (int y = -5; y <= 5; y++) {
                    for (int z = -10; z <= 10; z++) {
                        net.minecraft.core.BlockPos checkPos = mobPos.offset(x, y, z);
                        if (ghastly.level().getBlockState(checkPos).is(com.benji.netherman.NetherExp.GHASTLY_NEST.get())) {
                            targetHive = checkPos;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        ghastly.getNavigation().moveTo(targetHive.getX() + 0.5, targetHive.getY() + 0.5, targetHive.getZ() + 0.5, 1.2);
    }

    @Override
    public void tick() {
        if (targetHive != null) {
            double dist = ghastly.distanceToSqr(targetHive.getX() + 0.5, targetHive.getY() + 0.5, targetHive.getZ() + 0.5);
            if (dist < 2.0) {
                net.minecraft.world.level.block.entity.BlockEntity be = ghastly.level().getBlockEntity(targetHive);
                if (be instanceof com.benji.netherman.block.entity.GhastlyNestBlockEntity nest) {
                    if (nest.addGhastly(ghastly)) {
                        ghastly.level().playSound(null, targetHive, net.minecraft.sounds.SoundEvents.BEEHIVE_ENTER, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
                targetHive = null;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetHive != null && ghastly.level().getBlockState(targetHive).is(com.benji.netherman.NetherExp.GHASTLY_NEST.get());
    }
}
