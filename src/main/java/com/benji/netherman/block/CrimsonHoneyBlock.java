package com.benji.netherman.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrimsonHoneyBlock extends HalfTransparentBlock {
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public CrimsonHoneyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
    }
    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(level, entity);
        } else {
            this.bounceUpAndBack(entity);
        }
    }

    private void bounceUpAndBack(Entity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < 0.0D) {
            double dx = entity.getX() - entity.xo;
            double dz = entity.getZ() - entity.zo;
            double bounceY = -vec3.y * 2.0D;
            double pushX = -dx * 8.0D;
            double pushZ = -dz * 8.0D;

            entity.setDeltaMovement(pushX, bounceY, pushZ);
            entity.hurtMarked = true; 
        }
    }
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.isSuppressingBounce()) return;
        double entityBottom = entity.getY();
        double blockTop = pos.getY() + 1.0D;

        if (entityBottom < blockTop - 0.1D) {
            double dx = entity.getX() - entity.xo;
            double dz = entity.getZ() - entity.zo;
            if (Math.abs(dx) > 0.05D || Math.abs(dz) > 0.05D) {
                entity.setDeltaMovement(-dx * 4.0D, 0.6D, -dz * 4.0D);
                entity.hurtMarked = true;
            }
        }

        super.entityInside(state, level, pos, entity);
    }
}