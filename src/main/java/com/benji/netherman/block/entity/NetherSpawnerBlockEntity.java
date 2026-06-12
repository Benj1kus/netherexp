package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class NetherSpawnerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int spawnCooldown = 0;

    public NetherSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.NETHER_SPAWNER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NetherSpawnerBlockEntity entity) {
        if (level.isClientSide) return;

        if (entity.spawnCooldown > 0) {
            entity.spawnCooldown--;
            return;
        }

        // Проверяем раз в полсекунды для оптимизации TPS
        if (level.getGameTime() % 10 == 0) {
            // УВЕЛИЧЕННЫЙ РАДИУС ТРИГГЕРА: 40 БЛОКОВ
            Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 40.0D, false);
            if (player != null) {

                BlockPos[] neighbors = {pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below()};

                boolean spawnManipulator = false;
                boolean spawnGuardian = false;
                boolean spawnWelcomer = false;
                boolean spawnBelievers = false;
                boolean spawnBlacksmith = false;
                boolean spawnDoctor = false;
                boolean spawnGolem = false;
                boolean spawnTrader = false;

                // Сканируем соседние блоки
                for (BlockPos neighborPos : neighbors) {
                    Block block = level.getBlockState(neighborPos).getBlock();
                    if (block == Blocks.GILDED_BLACKSTONE) spawnManipulator = true;
                    else if (block == Blocks.POLISHED_BLACKSTONE_BRICKS) spawnGuardian = true;
                    else if (block == Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS) spawnWelcomer = true;
                    else if (block == Blocks.EMERALD_BLOCK) spawnBelievers = true;
                    else if (block == Blocks.IRON_BLOCK) spawnBlacksmith = true;
                    else if (block == Blocks.LAPIS_BLOCK) spawnDoctor = true;
                    else if (block == Blocks.RAW_GOLD_BLOCK) spawnGolem = true;
                    else if (block == Blocks.DIAMOND_BLOCK) spawnTrader = true;
                }

                if (spawnManipulator) {
                    spawnSingleEntity(level, pos, player, NetherExp.MANIPULATOR.get().create(level), entity, 18000);
                } else if (spawnGuardian) {
                    GuardianEntity guardian = NetherExp.GUARDIAN.get().create(level);
                    if (guardian != null) guardian.startSpawning();
                    spawnSingleEntity(level, pos, player, guardian, entity, 18000);
                } else if (spawnWelcomer) {
                    WelcomerEntity welcomer = NetherExp.WELCOMER.get().create(level);
                    if (welcomer != null) welcomer.startSpawning();
                    spawnSingleEntity(level, pos, player, welcomer, entity, 18000);
                }
                // --- НОВЫЕ МОБЫ ---
                else if (spawnBlacksmith) {
                    spawnSingleEntity(level, pos, player, NetherExp.BLACKSMITH.get().create(level), entity, 72000);
                } else if (spawnDoctor) {
                    spawnSingleEntity(level, pos, player, NetherExp.DOCTOR.get().create(level), entity, 72000);
                } else if (spawnGolem) {
                    spawnSingleEntity(level, pos, player, NetherExp.GILDED_GOLEM.get().create(level), entity, 72000);
                } else if (spawnTrader) {
                    spawnSingleEntity(level, pos, player, NetherExp.TRADER.get().create(level), entity, 72000);
                }
                else if (spawnBelievers) {
                    // ЗАЩИТА TPS: Считаем сектантов в радиусе 15 блоков
                    List<BelieverEntity> currentBelievers = level.getEntitiesOfClass(BelieverEntity.class, new AABB(pos).inflate(15.0D));

                    if (currentBelievers.size() < 5) {
                        for (int i = 0; i < 5; i++) {
                            BelieverEntity believer = NetherExp.BELIEVER.get().create(level);
                            if (believer != null) {
                                // Случайный сдвиг в радиусе ~3 блоков от спавнера
                                double offsetX = (level.random.nextDouble() - 0.5) * 6.0;
                                double offsetZ = (level.random.nextDouble() - 0.5) * 6.0;

                                believer.moveTo(pos.getX() + 0.5 + offsetX, pos.getY() + 1.0, pos.getZ() + 0.5 + offsetZ, level.random.nextFloat() * 360F, 0);
                                level.addFreshEntity(believer);
                            }
                        }
                        spawnRedstoneParticles((ServerLevel) level, pos, 50, 3.0); // Широкое облако частиц
                        entity.spawnCooldown = 72000; // Кулдаун 1 час (20 тиков * 60 сек * 60 мин)
                    } else {
                        // Если в комнате УЖЕ есть 5 сектантов, откладываем проверку на 30 секунд
                        entity.spawnCooldown = 600;
                    }
                }
            }
        }
    }

    // --- УНИВЕРСАЛЬНЫЙ МЕТОД ДЛЯ СПАВНА 1 МОБА ---
    private static void spawnSingleEntity(Level level, BlockPos pos, Player player, Mob mob, NetherSpawnerBlockEntity entity, int cooldown) {
        if (mob != null) {
            mob.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);

            // Поворачиваем моба лицом к игроку
            double dx = player.getX() - (pos.getX() + 0.5);
            double dz = player.getZ() - (pos.getZ() + 0.5);
            float yRot = (float) (Math.atan2(-dx, dz) * (180D / Math.PI));
            mob.setYRot(yRot);
            mob.setYHeadRot(yRot);
            mob.yBodyRot = yRot;

            level.addFreshEntity(mob);

            if (level instanceof ServerLevel serverLevel) {
                spawnRedstoneParticles(serverLevel, pos, 30, 0.5);
            }

            entity.spawnCooldown = cooldown; // Применяем кулдаун (например, 72000 = 1 час)
        }
    }

    // --- МЕТОД ДЛЯ КРАСНЫХ ЧАСТИЦ ---
    private static void spawnRedstoneParticles(ServerLevel level, BlockPos pos, int count, double spread) {
        level.sendParticles(DustParticleOptions.REDSTONE,
                pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                count,
                spread, 1.0, spread,
                0.0);
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