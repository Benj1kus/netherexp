package com.benji.netherman.block;

import com.benji.netherman.block.entity.PointedBlackstoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class PointedBlackstoneBlock extends PointedDripstoneBlock implements EntityBlock {

    public PointedBlackstoneBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PointedBlackstoneBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // --- ПЕРЕОПРЕДЕЛЯЕМ ЛОГИКУ СОЕДИНЕНИЯ ---

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction dir = context.getNearestLookingVerticalDirection().getOpposite();
        Direction placementDir = context.getClickedFace();
        boolean isTipMerge = false;

        // Проверяем, ставим ли мы капельник кончик-к-кончику
        if (placementDir.getAxis() == Direction.Axis.Y) {
            BlockState stateTarget = level.getBlockState(pos.relative(placementDir.getOpposite()));
            if (isPointedBlackstoneWithDirection(stateTarget, placementDir)) {
                dir = placementDir;
                isTipMerge = true;
            }
        }

        BlockState state = this.defaultBlockState().setValue(TIP_DIRECTION, dir).setValue(THICKNESS, DripstoneThickness.TIP);
        DripstoneThickness thickness = calculateThickness(level, pos, dir, isTipMerge);
        return state.setValue(THICKNESS, thickness);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Поддержка воды
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (dir != Direction.UP && dir != Direction.DOWN) {
            return state;
        } else {
            Direction tipDir = state.getValue(TIP_DIRECTION);
            if (tipDir == Direction.DOWN && level.getBlockTicks().hasScheduledTick(pos, this)) {
                return state; // Ждем падения
            } else if (dir == tipDir.getOpposite() && !this.canSurvive(state, level, pos)) {
                destroyChain(level, pos, tipDir);
                return Blocks.AIR.defaultBlockState();
            } else {
                // ПЕРЕСЧИТЫВАЕМ ТОЛЩИНУ ПРИ ИЗМЕНЕНИИ СОСЕДЕЙ
                boolean isTipMerge = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
                DripstoneThickness newThickness = calculateThickness(level, pos, tipDir, isTipMerge);
                return state.setValue(THICKNESS, newThickness);
            }
        }
    }

    // --- ПЕРЕОПРЕДЕЛЯЕМ ПРОВЕРКУ ОПОРЫ (ЧТОБЫ МОЖНО БЫЛО СТАВИТЬ ДРУГ НА ДРУГА) ---

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction tipDirection = state.getValue(TIP_DIRECTION);
        BlockPos supportPos = pos.relative(tipDirection.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);

        // Блок может выжить, если он прикреплен к твердой поверхности
        // ИЛИ если он прикреплен к нашему PointedBlackstoneBlock, который смотрит в ту же сторону
        return supportState.isFaceSturdy(level, supportPos, tipDirection) || isPointedBlackstoneWithDirection(supportState, tipDirection);
    }

    // Вспомогательный метод: проверяет, является ли блок именно НАШИМ капельником
    private boolean isPointedBlackstoneWithDirection(BlockState state, Direction dir) {
        return state.is(this) && state.hasProperty(TIP_DIRECTION) && state.getValue(TIP_DIRECTION) == dir;
    }

    private void destroyChain(LevelAccessor level, BlockPos pos, Direction dir) {

        BlockPos.MutableBlockPos mutable = pos.mutable();

        // Ищем конец цепи
        while (true) {

            BlockState state = level.getBlockState(mutable);

            if (!isPointedBlackstoneWithDirection(state, dir)) {
                mutable.move(dir.getOpposite());
                break;
            }

            mutable.move(dir);
        }

        // Ломаем с конца к основанию
        while (true) {

            BlockState state = level.getBlockState(mutable);

            if (!isPointedBlackstoneWithDirection(state, dir)) {
                break;
            }

            level.destroyBlock(mutable, true);

            mutable.move(dir.getOpposite());
        }
    }

// --- ИСПРАВЛЕННАЯ ЛОГИКА ВЫЧИСЛЕНИЯ ТОЛЩИНЫ ---

// --- ИСПРАВЛЕННАЯ ЛОГИКА ВЫЧИСЛЕНИЯ ТОЛЩИНЫ ---

    private DripstoneThickness calculateThickness(LevelReader level, BlockPos pos, Direction dir, boolean isPlacementMerge) {
        Direction opposite = dir.getOpposite();

        // Блок перед нами (в сторону роста капельника)
        BlockState stateFront = level.getBlockState(pos.relative(dir));
        // Блок позади нас (ближе к стене/потолку)
        BlockState stateBehind = level.getBlockState(pos.relative(opposite));

        boolean hasFront = isPointedBlackstoneWithDirection(stateFront, dir);
        boolean hasBehind = isPointedBlackstoneWithDirection(stateBehind, dir);

        if (!hasFront) {
            // Впереди НЕТ нашего капельника -> значит, мы на самом конце.
            // Проверяем, не упираемся ли мы во ВСТРЕЧНЫЙ капельник (слияние)
            boolean isMerge = isPlacementMerge || isPointedBlackstoneWithDirection(stateFront, opposite);
            return isMerge ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP;

        } else if (!hasBehind) {
            // Впереди есть капельник, а сзади нет (мы крепимся к стене) -> Основание
            return DripstoneThickness.BASE;

        } else {
            // Капельники есть и спереди, и сзади. Выбираем между MIDDLE и FRUSTUM.
            // Заглядываем на 2 блока вперед (за тот блок, что перед нами)
            BlockState stateFrontOfFront = level.getBlockState(pos.relative(dir, 2));
            boolean frontHasFront = isPointedBlackstoneWithDirection(stateFrontOfFront, dir);

            // Если у блока перед нами ЕСТЬ продолжение, то мы глубоко в середине (MIDDLE).
            // Если у блока перед нами НЕТ продолжения (он кончик), то мы пред-кончик (FRUSTUM).
            return frontHasFront ? DripstoneThickness.MIDDLE : DripstoneThickness.FRUSTUM;
        }
    }
}