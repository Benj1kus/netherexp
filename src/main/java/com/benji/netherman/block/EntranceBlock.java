package com.benji.netherman.block;

import com.benji.netherman.block.entity.EntranceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class EntranceBlock extends Block implements EntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public EntranceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(OPEN) ? Shapes.empty() : SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (state.getValue(OPEN)) return InteractionResult.PASS;
            Map<BlockPos, Integer> distances = new HashMap<>();
            Queue<BlockPos> queue = new LinkedList<>();

            queue.add(pos);
            distances.put(pos, 0);
            int maxDist = 0;
            while (!queue.isEmpty() && distances.size() < 1000) {
                BlockPos current = queue.poll();
                int currentDist = distances.get(current);
                maxDist = Math.max(maxDist, currentDist);
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);

                    if (!distances.containsKey(neighbor) && level.getBlockState(neighbor).is(this)) {
                        distances.put(neighbor, currentDist + 1);
                        queue.add(neighbor);
                    }
                }
            }
            for (Map.Entry<BlockPos, Integer> entry : distances.entrySet()) {
                if (level.getBlockEntity(entry.getKey()) instanceof EntranceBlockEntity be) {
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
        return new EntranceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, p, st, entity) -> {
            if (entity instanceof EntranceBlockEntity be) EntranceBlockEntity.tick(lvl, p, st, be);
        };
    }
}