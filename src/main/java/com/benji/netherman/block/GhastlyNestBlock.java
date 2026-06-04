package com.benji.netherman.block;

import com.benji.netherman.block.entity.GhastlyNestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.benji.netherman.NetherExp;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class GhastlyNestBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_HONEY = BooleanProperty.create("has_honey");

    public GhastlyNestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_HONEY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_HONEY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GhastlyNestBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // Используем стандартный рендер по JSON
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Если игрок кликает пустой бутылочкой и в улье есть мёд
        if (itemstack.is(Items.GLASS_BOTTLE) && state.getValue(HAS_HONEY)) {
            if (!level.isClientSide()) {
                // Тратим 1 пустую бутылочку
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                // Звук набора мёда
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);

                // Выдаем игроку Crimson Honey Bottle
                ItemStack honeyBottle = new ItemStack(NetherExp.CRIMSON_HONEY_BOTTLE.get());
                if (itemstack.isEmpty()) {
                    player.setItemInHand(hand, honeyBottle);
                } else if (!player.getInventory().add(honeyBottle)) {
                    player.drop(honeyBottle, false);
                }

                // Опустошаем улей (сбрасываем текстуру на пустую)
                level.setBlock(pos, state.setValue(HAS_HONEY, false), 3);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, com.benji.netherman.NetherExp.GHASTLY_NEST_BE.get(), GhastlyNestBlockEntity::serverTick);
    }

    // Если блок ломают - выпускаем всех сохраненных гастли наружу!
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof GhastlyNestBlockEntity nest) {
                nest.releaseAllGhastlies();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}