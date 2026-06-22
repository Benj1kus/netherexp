package com.benji.netherman.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StatueStandBlock extends HorizontalDirectionalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public StatueStandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)); // По умолчанию - нижняя часть
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    // 1. Проверяем, есть ли место ДЛЯ ДВУХ БЛОКОВ при установке
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        // Если над устанавливаемым блоком нет места (например, потолок в 1 блок высотой) - запрещаем установку
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return null;
    }

    // 2. Сразу после установки нижней части, мы насильно ставим верхнюю часть над ней
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    // 3. Логика разрушения: если ломается одна часть, ломается и вторая
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        // Проверяем блок сверху (если мы низ) или снизу (если мы верх)
        if (facing.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (facing == Direction.UP)) {
            // Если второго куска больше нет, ломаем и этот
            return facingState.is(this) && facingState.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        return half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            DoubleBlockHalf half = state.getValue(HALF);

            // Если игрок ломает ВЕРХНЮЮ половину в выживании
            if (half == DoubleBlockHalf.UPPER) {
                BlockPos blockpos = pos.below();
                BlockState blockstate = level.getBlockState(blockpos);

                // Проверяем, что снизу реально находится нижняя часть нашего блока
                if (blockstate.is(state.getBlock()) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    if (player.isCreative()) {
                        // В креативе просто убираем нижний блок без звуков и дропа
                        level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                        level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
                    } else {
                        // В выживании принудительно ломаем НИЖНИЙ блок (он и выдаст предмет)
                        level.destroyBlock(blockpos, true);
                    }
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }


    // Проверяем, может ли блок здесь существовать
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
        return SHAPE; // Хитбокс всегда 16 пикселей для каждой половинки
    }
}