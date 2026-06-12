package com.benji.netherman.block;

import com.benji.netherman.block.entity.PointedBlackstoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class PointedBlackstoneBlock extends PointedDripstoneBlock implements EntityBlock {

    public PointedBlackstoneBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PointedBlackstoneBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction dir = context.getNearestLookingVerticalDirection().getOpposite();
        Direction placementDir = context.getClickedFace();
        boolean isTipMerge = false;
        if (placementDir.getAxis() == Direction.Axis.Y) {
            BlockState stateTarget = level.getBlockState(pos.relative(placementDir.getOpposite()));
            if (isPointedBlackstoneWithDirection(stateTarget, placementDir)) {
                dir = placementDir;
                isTipMerge = true;
            }
        }

        BlockState state = this.defaultBlockState().setValue(TIP_DIRECTION, dir).setValue(THICKNESS, DripstoneThickness.TIP);
        DripstoneThickness thickness = calculateThickness(level, pos, dir, isTipMerge);
        return state.setValue(THICKNESS, thickness);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (dir != Direction.UP && dir != Direction.DOWN) {
            return state;
        } else {
            Direction tipDir = state.getValue(TIP_DIRECTION);
            if (tipDir == Direction.DOWN && level.getBlockTicks().hasScheduledTick(pos, this)) {
                return state;
            } else if (dir == tipDir.getOpposite() && !this.canSurvive(state, level, pos)) {
                destroyChain(level, pos, tipDir);
                return Blocks.AIR.defaultBlockState();
            } else {
                boolean isTipMerge = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
                DripstoneThickness newThickness = calculateThickness(level, pos, tipDir, isTipMerge);
                return state.setValue(THICKNESS, newThickness);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction tipDirection = state.getValue(TIP_DIRECTION);
        BlockPos supportPos = pos.relative(tipDirection.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, tipDirection) || isPointedBlackstoneWithDirection(supportState, tipDirection);
    }
    private boolean isPointedBlackstoneWithDirection(BlockState state, Direction dir) {
        return state.is(this) && state.hasProperty(TIP_DIRECTION) && state.getValue(TIP_DIRECTION) == dir;
    }

    private void destroyChain(LevelAccessor level, BlockPos pos, Direction dir) {

        BlockPos.MutableBlockPos mutable = pos.mutable();
        while (true) {

            BlockState state = level.getBlockState(mutable);

            if (!isPointedBlackstoneWithDirection(state, dir)) {
                mutable.move(dir.getOpposite());
                break;
            }

            mutable.move(dir);
        }
        while (true) {

            BlockState state = level.getBlockState(mutable);

            if (!isPointedBlackstoneWithDirection(state, dir)) {
                break;
            }

            level.destroyBlock(mutable, true);

            mutable.move(dir.getOpposite());
        }
    }

    private DripstoneThickness calculateThickness(LevelReader level, BlockPos pos, Direction dir, boolean isPlacementMerge) {
        Direction opposite = dir.getOpposite();
        BlockState stateFront = level.getBlockState(pos.relative(dir));
        BlockState stateBehind = level.getBlockState(pos.relative(opposite));

        boolean hasFront = isPointedBlackstoneWithDirection(stateFront, dir);
        boolean hasBehind = isPointedBlackstoneWithDirection(stateBehind, dir);

        if (!hasFront) {
            boolean isMerge = isPlacementMerge || isPointedBlackstoneWithDirection(stateFront, opposite);
            return isMerge ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP;

        } else if (!hasBehind) {
            return DripstoneThickness.BASE;

        } else {
            BlockState stateFrontOfFront = level.getBlockState(pos.relative(dir, 2));
            boolean frontHasFront = isPointedBlackstoneWithDirection(stateFrontOfFront, dir);
            return frontHasFront ? DripstoneThickness.MIDDLE : DripstoneThickness.FRUSTUM;
        }
    }
}