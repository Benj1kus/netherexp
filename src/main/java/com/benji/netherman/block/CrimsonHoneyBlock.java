package com.benji.netherman.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrimsonHoneyBlock extends HalfTransparentBlock {
    // Хитбокс 14x14x14 (как у ванильного мёда), чтобы игрок мог "тереться" о бока и мы могли это отслеживать
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public CrimsonHoneyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // 1. Отключаем ванильный урон от падения (как у слизи/мёда)
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
    }

    // 2. ОТСКОК ПРИ ПАДЕНИИ СВЕРХУ
    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityAfterFallOn(level, entity);
        } else {
            this.bounceUpAndBack(entity);
        }
    }

    private void bounceUpAndBack(Entity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < 0.0D) {
            // Высчитываем реальное смещение сущности за предыдущий тик
            double dx = entity.getX() - entity.xo;
            double dz = entity.getZ() - entity.zo;

            // Подкидываем вверх (зависит от силы падения)
            double bounceY = -vec3.y * 2.0D;

            // Инвертируем X и Z, чтобы откинуть НАЗАД по дуге, умножая силу (1.5)
            double pushX = -dx * 8.0D;
            double pushZ = -dz * 8.0D;

            entity.setDeltaMovement(pushX, bounceY, pushZ);
            entity.hurtMarked = true; // Принудительно обновляем вектор движения на клиенте
        }
    }

    // 3. ОТСКОК ПРИ СТОЛКНОВЕНИИ СО СТЕНОЙ
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.isSuppressingBounce()) return;

        // Проверяем, что сущность столкнулась именно сбоку, а не просто падает на верхнюю грань
        double entityBottom = entity.getY();
        double blockTop = pos.getY() + 1.0D;

        if (entityBottom < blockTop - 0.1D) {
            double dx = entity.getX() - entity.xo;
            double dz = entity.getZ() - entity.zo;

            // Если есть горизонтальное движение (врезался в блок на скорости)
            if (Math.abs(dx) > 0.05D || Math.abs(dz) > 0.05D) {
                // Откидываем назад и подкидываем вверх по красивой дуге (Y = 0.6)
                entity.setDeltaMovement(-dx * 4.0D, 0.6D, -dz * 4.0D);
                entity.hurtMarked = true;
            }
        }

        super.entityInside(state, level, pos, entity);
    }
}