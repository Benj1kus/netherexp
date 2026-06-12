package com.benji.netherman.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.EnumSet;

public class GhastlyPollinateGoal extends Goal {
    private final GhastlyEntity ghastly;
    private BlockPos targetPos = null;
    private int hoverTicks = 0;
    private int cooldown = 0;

    public GhastlyPollinateGoal(GhastlyEntity ghastly) {
        this.ghastly = ghastly;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (ghastly.isTame() || ghastly.getEntityData().get(GhastlyEntity.SHOW_HINT)) return false;

        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (ghastly.getRandom().nextInt(20) == 0) {
            BlockPos mobPos = ghastly.blockPosition();
            for (int x = -8; x <= 8; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -8; z <= 8; z++) {
                        BlockPos checkPos = mobPos.offset(x, y, z);
                        Block block = ghastly.level().getBlockState(checkPos).getBlock();
                        if (block == Blocks.CRIMSON_ROOTS || block == Blocks.WARPED_ROOTS) {
                            targetPos = checkPos;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    @Override
    public boolean canContinueToUse() {
        if (ghastly.isTame() || ghastly.getEntityData().get(GhastlyEntity.SHOW_HINT)) return false;
        if (targetPos == null) return false;
        Block block = ghastly.level().getBlockState(targetPos).getBlock();
        return block == Blocks.CRIMSON_ROOTS || block == Blocks.WARPED_ROOTS;
    }

    @Override
    public void start() {
        hoverTicks = 0;
    }

    @Override
    public void tick() {
        if (targetPos == null) return;
        double dist = ghastly.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5);

        if (dist > 2.0) {
            ghastly.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY() + 1.2, targetPos.getZ() + 0.5, 1.0);
        } else {
            ghastly.getNavigation().stop();
            ghastly.setDeltaMovement(ghastly.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D));

            hoverTicks++;
            if (hoverTicks >= 40) {
                performAction();
                targetPos = null;
                cooldown = 200;
            }
        }
    }
    @Override
    public void stop() {
        targetPos = null;
        hoverTicks = 0;
        ghastly.getNavigation().stop();
    }

    private void performAction() {
        Block block = ghastly.level().getBlockState(targetPos).getBlock();
        boolean isEat = ghastly.getRandom().nextBoolean();

        if (isEat) {
            ghastly.getEntityData().set(GhastlyEntity.IS_EATING, true);
            ghastly.eatTicks = 60;
            ghastly.level().destroyBlock(targetPos, false);
        } else {
            if (ghastly.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.0);
                int toPlant = 2 + ghastly.getRandom().nextInt(2);
                for (int i = 0; i < 10 && toPlant > 0; i++) {
                    BlockPos p = targetPos.offset(ghastly.getRandom().nextInt(5) - 2, ghastly.getRandom().nextInt(3) - 1, ghastly.getRandom().nextInt(5) - 2);
                    if (ghastly.level().isEmptyBlock(p) && ghastly.level().getBlockState(p.below()).isSolidRender(ghastly.level(), p.below())) {
                        ghastly.level().setBlockAndUpdate(p, block.defaultBlockState());
                        toPlant--;
                    }
                }
            }
        }
    }
}