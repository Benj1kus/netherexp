package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import com.benji.netherman.config.AzazelConfig;
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

public class NetherSpawnerBlockEntity extends BlockEntity {
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

        if (level.getGameTime() % 10 == 0) {
            double detectionRadius = AzazelConfig.PLAYER_DETECTION_RADIUS.get();
            Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), detectionRadius, false);
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

                int miniBossCD = AzazelConfig.MINI_BOSS_COOLDOWN.get();
                int civilianCD = AzazelConfig.CIVILIAN_NPC_COOLDOWN.get();

                if (spawnManipulator) {
                    spawnSingleEntity(level, pos, player, NetherExp.MANIPULATOR.get().create(level), entity, miniBossCD);
                } else if (spawnGuardian) {
                    GuardianEntity guardian = NetherExp.GUARDIAN.get().create(level);
                    if (guardian != null) guardian.startSpawning();
                    spawnSingleEntity(level, pos, player, guardian, entity, miniBossCD);
                } else if (spawnWelcomer) {
                    WelcomerEntity welcomer = NetherExp.WELCOMER.get().create(level);
                    if (welcomer != null) welcomer.startSpawning();
                    spawnSingleEntity(level, pos, player, welcomer, entity, miniBossCD);
                } else if (spawnBlacksmith) {
                    spawnSingleEntity(level, pos, player, NetherExp.BLACKSMITH.get().create(level), entity, civilianCD);
                } else if (spawnDoctor) {
                    spawnSingleEntity(level, pos, player, NetherExp.DOCTOR.get().create(level), entity, civilianCD);
                } else if (spawnGolem) {
                    spawnSingleEntity(level, pos, player, NetherExp.GILDED_GOLEM.get().create(level), entity, civilianCD);
                } else if (spawnTrader) {
                    spawnSingleEntity(level, pos, player, NetherExp.TRADER.get().create(level), entity, civilianCD);
                } else if (spawnBelievers) {

                    double checkRadius = AzazelConfig.BELIEVERS_SPAWN_RADIUS.get() + 9.0D; // Оставляем пропорцию зоны проверки
                    List<BelieverEntity> currentBelievers = level.getEntitiesOfClass(BelieverEntity.class, new AABB(pos).inflate(checkRadius));
                    int maxAllowed = AzazelConfig.BELIEVERS_MAX_NEARBY.get();

                    if (currentBelievers.size() < maxAllowed) {
                        int spawnCount = AzazelConfig.BELIEVERS_SPAWN_COUNT.get();
                        double spawnRadius = AzazelConfig.BELIEVERS_SPAWN_RADIUS.get();

                        for (int i = 0; i < spawnCount; i++) {
                            BelieverEntity believer = NetherExp.BELIEVER.get().create(level);
                            if (believer != null) {
                                double offsetX = (level.random.nextDouble() - 0.5) * spawnRadius;
                                double offsetZ = (level.random.nextDouble() - 0.5) * spawnRadius;

                                believer.moveTo(pos.getX() + 0.5 + offsetX, pos.getY() + 1.0, pos.getZ() + 0.5 + offsetZ, level.random.nextFloat() * 360F, 0);
                                level.addFreshEntity(believer);
                            }
                        }
                        spawnRedstoneParticles((ServerLevel) level, pos, 50, spawnRadius / 2.0);
                        entity.spawnCooldown = AzazelConfig.BELIEVERS_SUCCESS_COOLDOWN.get();
                    } else {
                        entity.spawnCooldown = AzazelConfig.BELIEVERS_FAIL_COOLDOWN.get();
                    }
                }
            }
        }
    }
    private static void spawnSingleEntity(Level level, BlockPos pos, Player player, Mob mob, NetherSpawnerBlockEntity entity, int cooldown) {
        if (mob != null) {
            mob.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);
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

            entity.spawnCooldown = cooldown;
        }
    }
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
}