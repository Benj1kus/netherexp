package com.benji.netherman.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock; // ИЗМЕНЕНО
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CrimsonWebBlock extends DirectionalBlock {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    public static final BooleanProperty CLOSING = BooleanProperty.create("closing");

    private static final VoxelShape SHAPE_NS_0 = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);
    private static final VoxelShape SHAPE_NS_1 = Block.box(2.0D, 2.0D, 7.0D, 14.0D, 14.0D, 9.0D);
    private static final VoxelShape SHAPE_NS_2 = Block.box(5.0D, 5.0D, 7.0D, 11.0D, 11.0D, 9.0D);

    private static final VoxelShape SHAPE_EW_0 = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_EW_1 = Block.box(7.0D, 2.0D, 2.0D, 9.0D, 14.0D, 14.0D);
    private static final VoxelShape SHAPE_EW_2 = Block.box(7.0D, 5.0D, 5.0D, 9.0D, 11.0D, 11.0D);

    private static final VoxelShape SHAPE_Y_0 = Block.box(0.0D, 7.0D, 0.0D, 16.0D, 9.0D, 16.0D);
    private static final VoxelShape SHAPE_Y_1 = Block.box(2.0D, 7.0D, 2.0D, 14.0D, 9.0D, 14.0D);
    private static final VoxelShape SHAPE_Y_2 = Block.box(5.0D, 7.0D, 5.0D, 11.0D, 9.0D, 11.0D);

    public CrimsonWebBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STAGE, 0)
                .setValue(CLOSING, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE, CLOSING);
    }

    // ИЗМЕНЕНО: Логика установки с шифтом
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction dir;
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            dir = context.getClickedFace().getAxis() == Direction.Axis.Y ? context.getClickedFace() : Direction.UP;
        } else {
            dir = context.getHorizontalDirection().getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, dir);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int stage = state.getValue(STAGE);
        if (stage == 3) return Shapes.empty();

        Direction.Axis axis = state.getValue(FACING).getAxis();

        if (axis == Direction.Axis.Y) {
            return switch (stage) {
                case 1 -> SHAPE_Y_1;
                case 2 -> SHAPE_Y_2;
                default -> SHAPE_Y_0;
            };
        } else if (axis == Direction.Axis.X) {
            return switch (stage) {
                case 1 -> SHAPE_EW_1;
                case 2 -> SHAPE_EW_2;
                default -> SHAPE_EW_0;
            };
        } else {
            return switch (stage) {
                case 1 -> SHAPE_NS_1;
                case 2 -> SHAPE_NS_2;
                default -> SHAPE_NS_0;
            };
        }
    }

    // ... (Остальные методы use, tick, animateTick и getRenderShape остаются абсолютно без изменений)

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (state.getValue(STAGE) != 0) return InteractionResult.PASS;

            Map<BlockPos, Integer> distances = new HashMap<>();
            Queue<BlockPos> queue = new LinkedList<>();

            queue.add(pos);
            distances.put(pos, 0);

            while (!queue.isEmpty() && distances.size() < 1000) {
                BlockPos current = queue.poll();
                int currentDist = distances.get(current);

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!distances.containsKey(neighbor) && level.getBlockState(neighbor).is(this)) {
                        distances.put(neighbor, currentDist + 1);
                        queue.add(neighbor);
                    }
                }
            }

            for (Map.Entry<BlockPos, Integer> entry : distances.entrySet()) {
                int delay = (int) (Math.pow(entry.getValue(), 0.8) * 3);
                level.scheduleTick(entry.getKey(), this, Math.max(1, delay));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        boolean isClosing = state.getValue(CLOSING);

        if (!isClosing) {
            if (stage == 0) {
                level.playSound(null, pos, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 0.9F + random.nextFloat() * 0.2F);
                level.setBlock(pos, state.setValue(STAGE, 1), 3);
                level.scheduleTick(pos, this, 3);
            } else if (stage == 1) {
                level.setBlock(pos, state.setValue(STAGE, 2), 3);
                level.scheduleTick(pos, this, 3);
            } else if (stage == 2) {
                level.setBlock(pos, state.setValue(STAGE, 3), 3);
                level.scheduleTick(pos, this, 50);
                level.setBlock(pos, level.getBlockState(pos).setValue(CLOSING, true), 3);
            }
        } else {
            if (stage == 3) {
                level.playSound(null, pos, SoundEvents.SCULK_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 0.9F + random.nextFloat() * 0.2F);
                level.setBlock(pos, state.setValue(STAGE, 2), 3);
                level.scheduleTick(pos, this, 3);
            } else if (stage == 2) {
                level.setBlock(pos, state.setValue(STAGE, 1), 3);
                level.scheduleTick(pos, this, 3);
            } else if (stage == 1) {
                level.setBlock(pos, state.setValue(STAGE, 0).setValue(CLOSING, false), 3);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        if (stage == 1 || stage == 2) {
            for (int i = 0; i < 2; i++) {
                double px = pos.getX() + random.nextDouble();
                double py = pos.getY() + random.nextDouble();
                double pz = pos.getZ() + random.nextDouble();
                level.addParticle(ParticleTypes.CRIMSON_SPORE, px, py, pz, 0, 0.05D, 0);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}