package com.benji.netherman.block;

import com.benji.netherman.block.entity.VoidMidBlockEntity;
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

public class VoidMidBlock extends HorizontalDirectionalBlock implements EntityBlock {

    // Визуальный хитбокс, чтобы игрок мог выделить блок мышкой и сломать в креативе
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public VoidMidBlock(Properties properties) {
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
            livingEntity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        }
    }

    // --- ПАРТИКЛЫ НАД БЛОКОМ ---
// --- ПАРТИКЛЫ НАД БЛОКОМ ---
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Шанс 1 к 30. Если частиц всё еще много — ставь 50 или даже 100!
        // Если покажется маловато — снизь до 15 или 20.
        if (random.nextInt(40) == 0) {
            double x = pos.getX() + random.nextDouble();
            // Поднимаем частицы на случайную высоту от 0 до 4 блоков над тьмой
            double y = pos.getY() + random.nextDouble() * 4.0;
            double z = pos.getZ() + random.nextDouble();

            // ASH - это те самые серые пепельные частицы из Базальтовых Дельт.
            // Даем им маааленькую скорость вверх (0.01D)
            level.addParticle(ParticleTypes.ASH, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidMidBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}