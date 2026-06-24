package com.benji.netherman.block;

import com.benji.netherman.NetherExp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StatueStandBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public static final BooleanProperty SOLVED = BooleanProperty.create("solved");

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public StatueStandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(SOLVED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SOLVED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(SOLVED, false);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(SOLVED, false), 3);

        if (!level.isClientSide()) {
            int radius = 10;
            boolean isPuzzleZone = false;

            for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
                if (level.getBlockState(checkPos).is(NetherExp.MAZE_DOOR.get())) {
                    isPuzzleZone = true;
                    break;
                }
            }

            if (isPuzzleZone) {
                List<BlockPos> allStatuesInRoom = new ArrayList<>();
                boolean hasSolvedStatue = false;

                for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
                    BlockState st = level.getBlockState(checkPos);
                    if (st.is(this) && st.getValue(HALF) == DoubleBlockHalf.LOWER) {
                        allStatuesInRoom.add(checkPos.immutable());
                        if (st.getValue(SOLVED)) {
                            hasSolvedStatue = true;
                        }
                    }
                }
                if (hasSolvedStatue || allStatuesInRoom.size() < 2) return;
                boolean allPaired = true;

                for (BlockPos statPos : allStatuesInRoom) {
                    BlockState statState = level.getBlockState(statPos);
                    Direction facing = statState.getValue(FACING);
                    boolean hasPartner = false;

                    for (int i = 1; i <= 10; i++) {
                        BlockPos targetPos = statPos.relative(facing, i);
                        BlockState targetState = level.getBlockState(targetPos);

                        if (targetState.is(this) && targetState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                            if (targetState.getValue(FACING) == facing.getOpposite()) {
                                hasPartner = true;
                            }
                            break;
                        }

                        if (targetState.isSolidRender(level, targetPos)) {
                            break;
                        }
                    }

                    if (!hasPartner) {
                        allPaired = false;
                        break;
                    }
                }

                if (allPaired) {
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F);

                    ItemEntity key = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, new ItemStack(NetherExp.MAZE_KEY.get()));
                    level.addFreshEntity(key);

                    for (BlockPos statPos : allStatuesInRoom) {
                        BlockState statState = level.getBlockState(statPos);

                        level.setBlock(statPos, statState.setValue(SOLVED, true), 3);
                        level.setBlock(statPos.above(), statState.setValue(HALF, DoubleBlockHalf.UPPER).setValue(SOLVED, true), 3);

                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                                    statPos.getX() + 0.5, statPos.getY() + 1.0, statPos.getZ() + 0.5,
                                    30, 0.5, 0.5, 0.5, 0.1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (facing.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (facing == Direction.UP)) {
            return facingState.is(this) && facingState.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        return half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            DoubleBlockHalf half = state.getValue(HALF);
            if (half == DoubleBlockHalf.UPPER) {
                BlockPos blockpos = pos.below();
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.is(state.getBlock()) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    if (player.isCreative()) {
                        level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                        level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
                    } else {
                        level.destroyBlock(blockpos, true);
                    }
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return super.canSurvive(state, level, pos);
        } else {
            BlockState blockstate = level.getBlockState(pos.below());
            return blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}