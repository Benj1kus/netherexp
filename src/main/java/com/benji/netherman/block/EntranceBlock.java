package com.benji.netherman.block;

import com.benji.netherman.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

public class EntranceBlock extends Block {
    // STAGE: 0 = Закрыт, 1 = Начало сжатия, 2 = Почти сжат, 3 = Полностью открыт (проходимый)
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    public static final BooleanProperty CLOSING = BooleanProperty.create("closing");

    // Динамические хитбоксы для плавного уменьшения куба
    private static final VoxelShape SHAPE_0 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_1 = Block.box(2.0D, 2.0D, 2.0D, 14.0D, 14.0D, 14.0D);
    private static final VoxelShape SHAPE_2 = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);

    public EntranceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0).setValue(CLOSING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, CLOSING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(STAGE)) {
            case 1 -> SHAPE_1;
            case 2 -> SHAPE_2;
            case 3 -> Shapes.empty(); // Полностью проходим
            default -> SHAPE_0;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (state.getValue(STAGE) != 0) return InteractionResult.PASS;

            // Наш старый добрый алгоритм поиска соединенных блоков (BFS)
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

            // Вместо BE планируем тики в планировщике Майнкрафта
            for (Map.Entry<BlockPos, Integer> entry : distances.entrySet()) {
                BlockPos targetPos = entry.getKey();
                int distance = entry.getValue();

                // Рассчитываем стартовую задержку для запуска волны на основе дистанции
                int delay = (int) (Math.pow(distance, 0.8) * 3);

                // Заставляем игру обновить этот блок через 'delay' тиков
                level.scheduleTick(targetPos, this, Math.max(1, delay));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int currentStage = state.getValue(STAGE);
        boolean isClosing = state.getValue(CLOSING);

        if (!isClosing) {
            // Процесс ОТКРЫТИЯ (сжатия блока)
            if (currentStage == 0) {
                // Звук воспроизводим только в самом начале сжатия блока
                level.playSound(null, pos, ModSounds.ENTRANCE.get(), SoundSource.BLOCKS, 1.0F, 0.9F + random.nextFloat() * 0.2F);
                level.setBlock(pos, state.setValue(STAGE, 1), 3);
                level.scheduleTick(pos, this, 3); // Быстрый шаг анимации
            } else if (currentStage == 1) {
                level.setBlock(pos, state.setValue(STAGE, 2), 3);
                level.scheduleTick(pos, this, 3);
            } else if (currentStage == 2) {
                level.setBlock(pos, state.setValue(STAGE, 3), 3);

                // Блок открыт! Задаем задержку перед закрытием (например, 100 тиков = 5 секунд)
                level.scheduleTick(pos, this, 100);
                level.setBlock(pos, level.getBlockState(pos).setValue(CLOSING, true), 3); // Меняем вектор на закрытие
            }
        } else {
            // Процесс ЗАКРЫТИЯ (возвращения блока)
            if (currentStage == 3) {
                level.playSound(null, pos, ModSounds.ENTRANCE.get(), SoundSource.BLOCKS, 1.0F, 0.9F + random.nextFloat() * 0.2F);
                level.setBlock(pos, state.setValue(STAGE, 2), 3);
                level.scheduleTick(pos, this, 3);
            } else if (currentStage == 2) {
                level.setBlock(pos, state.setValue(STAGE, 1), 3);
                level.scheduleTick(pos, this, 3);
            } else if (currentStage == 1) {
                // Полностью вернулся в исходное состояние
                level.setBlock(pos, state.setValue(STAGE, 0).setValue(CLOSING, false), 3);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int stage = state.getValue(STAGE);
        // Рисуем партиклы редстоуна на клиенте, если блок находится в процессе изменения размера
        if (stage == 1 || stage == 2) {
            for (int i = 0; i < 2; i++) {
                double px = pos.getX() + random.nextDouble();
                double py = pos.getY() + random.nextDouble();
                double pz = pos.getZ() + random.nextDouble();
                level.addParticle(DustParticleOptions.REDSTONE, px, py, pz, 0, 0.05D, 0);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // Переключаем на рендер обычных JSON моделей!
    }
}