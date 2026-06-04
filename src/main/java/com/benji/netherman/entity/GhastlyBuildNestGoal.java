package com.benji.netherman.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import java.util.EnumSet;

public class GhastlyBuildNestGoal extends Goal {
    private final GhastlyEntity ghastly;
    private BlockPos targetStem = null;
    private BlockPos targetAirPos = null;

    public GhastlyBuildNestGoal(GhastlyEntity ghastly) {
        this.ghastly = ghastly;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    // Выносим проверку в отдельный точный метод, чтобы использовать его дважды
    private boolean isNestNearby(Level level, BlockPos center) {
        // Честный радиус в 30 блоков во все стороны без каких-либо пропусков координат
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-30, -10, -30), center.offset(30, 10, 30))) {
            if (level.getBlockState(pos).is(com.benji.netherman.NetherExp.GHASTLY_NEST.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canUse() {
        if (ghastly.isTame() || ghastly.eatTicks > 0) return false;

        // Проверяем раз в 5 секунд (100 тиков) для оптимизации
        if (ghastly.tickCount % 100 != 0) return false;

        Level level = ghastly.level();
        BlockPos mobPos = ghastly.blockPosition();

        // 1. Проверяем, нет ли уже гнезда в радиусе 30 блоков от самого моба
        if (isNestNearby(level, mobPos)) return false;

        // 2. Ищем багряное бревно в радиусе 20 блоков
        for (BlockPos pos : BlockPos.betweenClosed(mobPos.offset(-20, -10, -20), mobPos.offset(20, 10, 20))) {
            if (level.getBlockState(pos).is(Blocks.CRIMSON_STEM)) {
                for (Direction dir : Direction.values()) {
                    BlockPos airCheck = pos.relative(dir);
                    if (level.isEmptyBlock(airCheck)) {
                        this.targetStem = pos.immutable();
                        this.targetAirPos = airCheck.immutable();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void start() {
        if (targetAirPos != null) {
            ghastly.getNavigation().moveTo(targetAirPos.getX() + 0.5, targetAirPos.getY() + 0.5, targetAirPos.getZ() + 0.5, 1.2);
        }
    }

    @Override
    public void tick() {
        if (targetStem == null || targetAirPos == null) return;

        ghastly.getNavigation().moveTo(targetAirPos.getX() + 0.5, targetAirPos.getY() + 0.5, targetAirPos.getZ() + 0.5, 1.2);

        double dist = ghastly.distanceToSqr(targetAirPos.getX() + 0.5, targetAirPos.getY() + 0.5, targetAirPos.getZ() + 0.5);
        if (dist < 2.5) {
            ghastly.getNavigation().stop();
            Level level = ghastly.level();

            if (level.getBlockState(targetStem).is(Blocks.CRIMSON_STEM)) {

                // КРИТИЧЕСКИЙ ФИКС: Проверяем ульи вокруг бревна ЕЩЕ РАЗ прямо перед установкой блока!
                // Если пока мы летели, кто-то построился в радиусе 30 блоков — отменяем постройку.
                if (!isNestNearby(level, targetStem)) {
                    level.setBlockAndUpdate(targetStem, com.benji.netherman.NetherExp.GHASTLY_NEST.get().defaultBlockState());

                    level.playSound(null, targetStem, net.minecraft.sounds.SoundEvents.STEM_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.8F);
                    level.playSound(null, targetStem, net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.6F);

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, targetStem.getX() + 0.5, targetStem.getY() + 0.5, targetStem.getZ() + 0.5, 40, 0.6, 0.6, 0.6, 0.1);
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, targetStem.getX() + 0.5, targetStem.getY() + 0.5, targetStem.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.05);
                    }
                }
            }
            stop();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetStem != null && targetAirPos != null && ghastly.level().getBlockState(targetStem).is(Blocks.CRIMSON_STEM);
    }

    @Override
    public void stop() {
        this.targetStem = null;
        this.targetAirPos = null;
        ghastly.getNavigation().stop();
    }
}