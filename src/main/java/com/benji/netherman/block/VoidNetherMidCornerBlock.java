package com.benji.netherman.block;

import com.benji.netherman.block.entity.VoidMidBlockEntity;
import com.benji.netherman.block.entity.VoidMidCornerBlockEntity;
import com.benji.netherman.block.entity.VoidNetherMidCornerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class VoidNetherMidCornerBlock extends HorizontalDirectionalBlock implements EntityBlock {

    // Визуальный хитбокс, чтобы игрок мог выделить блок мышкой и сломать в креативе
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public VoidNetherMidCornerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Устанавливаем поворот блоку лицом к игроку
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // --- ОТКЛЮЧЕНИЕ ФИЗИЧЕСКОЙ КОЛЛИЗИИ ---
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // Игроки и мобы проходят насквозь
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // --- УРОН И ЭФФЕКТ ТЬМЫ ВНУТРИ БЛОКА ---
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity livingEntity) {

            // 1. Наносим урон (5.0F за удар, ванильная механика ограничит до 2 раз в сек = 10 урона/сек)
            livingEntity.hurt(level.damageSources().magic(), 5.0F);

            // 2. Накладываем эффект Тьмы (как у Вардена)
            // Параметры: Эффект, Длительность в тиках (60 тиков = 3 сек), Уровень (0), Партиклы от зелья (false), Иконка (false)
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, false));
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidNetherMidCornerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}