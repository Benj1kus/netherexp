package com.benji.netherman.block;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.MazeDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MazeDoorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public MazeDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        Direction right = state.getValue(FACING).getClockWise();

        for (int w = 0; w < 6; w++) {
            for (int h = 0; h < 5; h++) {
                if (w == 0 && h == 0) continue;
                BlockPos partPos = pos.relative(right, w).above(h);
                if (level.getBlockState(partPos).canBeReplaced()) {
                    level.setBlock(partPos, NetherExp.GRAND_DOOR_PART.get().defaultBlockState()
                            .setValue(FACING, state.getValue(FACING))
                            .setValue(OPEN, false), 3);
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof MazeDoorBlockEntity entity) {
            if (entity.getDoorState() != MazeDoorBlockEntity.STATE_CLOSED) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
                boolean foundPuzzle = false;
                int scanRadius = 7;

                for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-scanRadius, -scanRadius, -scanRadius), pos.offset(scanRadius, scanRadius, scanRadius))) {
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.is(NetherExp.STATUE_STAND.get()) ||
                            checkState.is(NetherExp.FACE_PUZZLE_LEFT_DOWN.get()) ||
                            checkState.is(NetherExp.TOTEMUS_HOLE.get())) {
                        foundPuzzle = true;
                        break;
                    }
                }

                entity.syncRequiresKey(foundPuzzle);

                if (foundPuzzle) {
                    ItemStack handStack = player.getItemInHand(hand);
                    if (handStack.is(NetherExp.MAZE_KEY.get())) {
                        handStack.shrink(1);
                        level.playSound(null, pos, ModSounds.HIRRING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        entity.openTemporary();
                    } else {
                        level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.5F); 
                        player.displayClientMessage(Component.translatable("message.netherman.door_locked"), true);
                    }
                } else {
                    entity.openTemporary();
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            Direction right = state.getValue(FACING).getClockWise();
            for (int w = 0; w < 6; w++) {
                for (int h = 0; h < 5; h++) {
                    if (w == 0 && h == 0) continue;
                    BlockPos partPos = pos.relative(right, w).above(h);
                    if (level.getBlockState(partPos).is(NetherExp.GRAND_DOOR_PART.get())) {
                        level.setBlock(partPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MazeDoorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == NetherExp.MAZE_DOOR_BE.get() ?
                (lvl, p, st, be) -> MazeDoorBlockEntity.tick(lvl, p, st, (MazeDoorBlockEntity) be) : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}