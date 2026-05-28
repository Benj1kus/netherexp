package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.GuardianEntity;
import com.benji.netherman.entity.ManipulatorEntity;
import com.benji.netherman.entity.WelcomerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NetherSpawnerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int spawnCooldown = 0; // Кулдаун спавна

    public NetherSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.NETHER_SPAWNER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NetherSpawnerBlockEntity entity) {
        if (level.isClientSide) return;

        if (entity.spawnCooldown > 0) {
            entity.spawnCooldown--;
            return; // Ждем завершения таймера
        }

        if (level.getGameTime() % 10 == 0) {
            Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10.0, false);
            if (player != null) {

                BlockPos[] neighbors = {pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below()};
                boolean spawnManipulator = false;
                boolean spawnGuardian = false;
                boolean spawnWelcomer = false;

                for (BlockPos neighborPos : neighbors) {
                    Block block = level.getBlockState(neighborPos).getBlock();
                    if (block == Blocks.GILDED_BLACKSTONE) spawnManipulator = true;
                    if (block == Blocks.POLISHED_BLACKSTONE_BRICKS) spawnGuardian = true;
                    if (block == Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS) spawnWelcomer = true;
                }

                // ПРИОРИТЕТ 1: МАНИПУЛЯТОР
                if (spawnManipulator) {
                    ManipulatorEntity manipulator = NetherExp.MANIPULATOR.get().create(level);
                    if (manipulator != null) {
                        manipulator.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);

                        // Поворачиваем босса к игроку
                        double dx = player.getX() - (pos.getX() + 0.5);
                        double dz = player.getZ() - (pos.getZ() + 0.5);
                        float yRot = (float) (Math.atan2(-dx, dz) * (180D / Math.PI));
                        manipulator.setYRot(yRot);
                        manipulator.setYHeadRot(yRot);
                        manipulator.yBodyRot = yRot;

                        level.addFreshEntity(manipulator);

                        // Эффект появления (облако красной пыли)
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(DustParticleOptions.REDSTONE,
                                    pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5,
                                    40, // Количество частиц
                                    0.5, 1.0, 0.5, // Разброс (ширина и высота)
                                    0.0); // Скорость
                        }

                        entity.spawnCooldown = 36000; // 30 минут кулдаун
                    }
                }
                // ПРИОРИТЕТ 2: ГАРДИАН
                else if (spawnGuardian) {
                    GuardianEntity guardian = NetherExp.GUARDIAN.get().create(level);
                    if (guardian != null) {
                        guardian.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);

                        double dx = player.getX() - (pos.getX() + 0.5);
                        double dz = player.getZ() - (pos.getZ() + 0.5);
                        float yRot = (float) (Math.atan2(-dx, dz) * (180D / Math.PI));
                        guardian.setYRot(yRot);
                        guardian.setYHeadRot(yRot);
                        guardian.yBodyRot = yRot;

                        guardian.startSpawning();
                        level.addFreshEntity(guardian);
                        entity.spawnCooldown = 36000;
                    }
                }
                // ПРИОРИТЕТ 3: ВЕЛКОМЕР
                else if (spawnWelcomer) {
                    WelcomerEntity welcomer = NetherExp.WELCOMER.get().create(level);
                    if (welcomer != null) {
                        welcomer.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);
                        welcomer.startSpawning();
                        level.addFreshEntity(welcomer);
                        entity.spawnCooldown = 36000;
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("SpawnCooldown", this.spawnCooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.spawnCooldown = tag.getInt("SpawnCooldown");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}