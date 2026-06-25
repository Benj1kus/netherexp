package com.benji.netherman.block;

import com.benji.netherman.NetherExp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SamsonitBlock extends Block {

    public SamsonitBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        updateNearbyBells(level, pos, true);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockState(fromPos).is(Blocks.BELL)) {
            updateNearbyBells(level, pos, true);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            updateNearbyBells(level, pos, false);
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private void updateNearbyBells(Level level, BlockPos myPos, boolean isAdding) {
        if (level.isClientSide()) return;

        for (Direction dir : Direction.values()) {
            BlockPos checkPos = myPos.relative(dir);
            BlockState targetState = level.getBlockState(checkPos);

            if (isAdding && targetState.is(Blocks.BELL)) {
                BlockState newState = NetherExp.SAMSONITE_BELL.get().defaultBlockState()
                        .setValue(BellBlock.FACING, targetState.getValue(BellBlock.FACING))
                        .setValue(BellBlock.ATTACHMENT, targetState.getValue(BellBlock.ATTACHMENT));
                level.setBlock(checkPos, newState, 3);
            }
            else if (!isAdding && targetState.is(NetherExp.SAMSONITE_BELL.get())) {
                boolean hasSamsonit = false;
                for (Direction dir2 : Direction.values()) {
                    if (level.getBlockState(checkPos.relative(dir2)).is(NetherExp.SAMSONIT.get())) {
                        hasSamsonit = true;
                        break;
                    }
                }
                if (!hasSamsonit) {
                    BlockState newState = Blocks.BELL.defaultBlockState()
                            .setValue(BellBlock.FACING, targetState.getValue(BellBlock.FACING))
                            .setValue(BellBlock.ATTACHMENT, targetState.getValue(BellBlock.ATTACHMENT));
                    level.setBlock(checkPos, newState, 3);
                }
            }
        }
    }
}