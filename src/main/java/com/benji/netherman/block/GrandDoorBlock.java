package com.benji.netherman.block;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.GrandDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.sounds.SoundSource;
import com.benji.netherman.ModSounds;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GrandDoorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private static final VoxelShape SHAPE_NS = Block.box(0.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D);
    private static final VoxelShape SHAPE_EW = Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 16.0D);

    public GrandDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Direction right = facing.getClockWise();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        for (int w = 0; w < 6; w++) {
            for (int h = 0; h < 12; h++) {
                if (w == 0 && h == 0) continue;
                BlockPos p = pos.relative(right, w).above(h);
                if (!level.getBlockState(p).canBeReplaced(context)) return null;
            }
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(OPEN, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction right = state.getValue(FACING).getClockWise();

        for (int w = 0; w < 6; w++) {
            for (int h = 0; h < 12; h++) {
                if (w == 0 && h == 0) continue;
                BlockPos p = pos.relative(right, w).above(h);
                level.setBlock(p, NetherExp.GRAND_DOOR_PART.get().defaultBlockState().setValue(FACING, state.getValue(FACING)).setValue(OPEN, false), 3);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            Direction right = state.getValue(FACING).getClockWise();
            for (int w = 0; w < 6; w++) {
                for (int h = 0; h < 12; h++) {
                    if (w == 0 && h == 0) continue;
                    BlockPos p = pos.relative(right, w).above(h);
                    if (level.getBlockState(p).is(NetherExp.GRAND_DOOR_PART.get())) {
                        level.destroyBlock(p, false);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(OPEN)) return Shapes.empty();
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_EW : SHAPE_NS;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (level.getBlockEntity(pos) instanceof GrandDoorBlockEntity door) {
                if (door.getDoorState() == GrandDoorBlockEntity.STATE_CLOSED) {
                    door.openTemporary();
                    player.displayClientMessage(Component.literal("§cGood Luck traveler!"), true);
                    level.playSound(null, pos, ModSounds.GOODLUCK.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GrandDoorBlockEntity(pos, state); }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    // ИСПРАВЛЕНИЕ: БЕЗОПАСНАЯ РЕГИСТРАЦИЯ ТИКЕРА
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == NetherExp.GRAND_DOOR_BE.get()) {
            return (lvl, pos, st, entity) -> GrandDoorBlockEntity.tick(lvl, pos, st, (GrandDoorBlockEntity) entity);
        }
        return null;
    }
}