package com.benji.netherman.block;

import com.benji.netherman.block.entity.CrimsonWebBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CrimsonWebBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private static final VoxelShape SHAPE_NS =
            Block.box(0.0D, 0.0D, 7.0D,
                    16.0D, 16.0D, 9.0D);

    private static final VoxelShape SHAPE_EW =
            Block.box(7.0D, 0.0D, 0.0D,
                    9.0D, 16.0D, 16.0D);

    public CrimsonWebBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(OPEN, false)
        );
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(OPEN, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        if (state.getValue(OPEN)) {
            return Shapes.empty();
        }

        return state.getValue(FACING).getAxis() == Direction.Axis.X
                ? SHAPE_EW
                : SHAPE_NS;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {

            // Если он уже открыт, игнорируем клик
            if (state.getValue(OPEN)) return InteractionResult.PASS;

            // --- АЛГОРИТМ BFS ДЛЯ ЭФФЕКТА ВОЛНЫ ---
            Map<BlockPos, Integer> distances = new HashMap<>();
            Queue<BlockPos> queue = new LinkedList<>();

            queue.add(pos);
            distances.put(pos, 0);
            int maxDist = 0;

            // Ограничение в 1000 блоков для безопасности сервера
            while (!queue.isEmpty() && distances.size() < 1000) {
                BlockPos current = queue.poll();
                int currentDist = distances.get(current);
                maxDist = Math.max(maxDist, currentDist);

                // Проверяем 6 соседей (верх, низ, север, юг, восток, запад)
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);

                    if (!distances.containsKey(neighbor) && level.getBlockState(neighbor).is(this)) {
                        distances.put(neighbor, currentDist + 1);
                        queue.add(neighbor);
                    }
                }
            }

            // Передаем команду всем найденным блокам
            for (Map.Entry<BlockPos, Integer> entry : distances.entrySet()) {
                if (level.getBlockEntity(entry.getKey()) instanceof CrimsonWebBlockEntity be) {
                    be.triggerWave(entry.getValue(), maxDist);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrimsonWebBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, entity) -> {
            if (entity instanceof CrimsonWebBlockEntity be) CrimsonWebBlockEntity.tick(lvl, p, st, be);
        };
    }
}