package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.AltarBlock;
import com.benji.netherman.block.LabyrinthTeleportBlock;
import com.benji.netherman.world.data.TeleportDestinationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.List;

public class LabyrinthTeleportBlockEntity extends BlockEntity {

    private int playerStandTimer = 0;

    public LabyrinthTeleportBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.LABYRINTH_TELEPORT_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LabyrinthTeleportBlockEntity entity) {
        int currentMode = state.getValue(LabyrinthTeleportBlock.MODE);

        if (!level.isClientSide() && level.getGameTime() % 20 == 0) {
            int totalAltars = 0;
            int guessedAltars = 0;

            for (int x = -20; x <= 20; x++) {
                for (int y = -20; y <= 20; y++) {
                    for (int z = -20; z <= 20; z++) {
                        BlockPos checkPos = pos.offset(x, y, z);
                        BlockState neighbor = level.getBlockState(checkPos);
                        if (neighbor.is(NetherExp.ALTAR.get())) {
                            totalAltars++;
                            if (neighbor.getValue(AltarBlock.GUESSED)) {
                                guessedAltars++;
                            }
                        }
                    }
                }
            }

            int newMode = 0;
            if (totalAltars == 0) {
                newMode = 2;
            } else if (guessedAltars >= 6) {
                newMode = 1;
            }

            if (newMode != currentMode) {
                level.setBlock(pos, state.setValue(LabyrinthTeleportBlock.MODE, newMode), 3);
            }

            if (newMode == 2) {
                TeleportDestinationData.get((ServerLevel) level).addDestination(pos);
            }
        }

        if (level.isClientSide() && currentMode == 1) {
            if (level.random.nextBoolean()) {
                DustParticleOptions redDust = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
                level.addParticle(redDust,
                        pos.getX() + level.random.nextFloat(),
                        pos.getY() + 1.0D + level.random.nextFloat() * 2.0D,
                        pos.getZ() + level.random.nextFloat(),
                        0.0D, 0.05D, 0.0D);
            }
        }

        if (!level.isClientSide() && currentMode == 1) {
            AABB detectionBox = new AABB(pos).move(0, 1, 0);
            List<Player> players = level.getEntitiesOfClass(Player.class, detectionBox);

            if (!players.isEmpty()) {
                entity.playerStandTimer++;

                if (entity.playerStandTimer >= 20) {
                    Player p = players.get(0);
                    BlockPos dest = TeleportDestinationData.get((ServerLevel) level).getRandomDestination(level.random);

                    if (dest != null && !dest.equals(pos)) {
                        p.teleportTo(dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5);

                        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.5F, 0.5F); // Приглушенный

                        level.playSound(null, dest, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.5F, 0.8F);
                        ((ServerLevel) level).sendParticles(ParticleTypes.REVERSE_PORTAL, dest.getX() + 0.5, dest.getY() + 1.5, dest.getZ() + 0.5, 100, 0.5, 1.0, 0.5, 0.2);
                        ((ServerLevel) level).sendParticles(ParticleTypes.SOUL_FIRE_FLAME, dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5, 50, 0.5, 0.2, 0.5, 0.1);
                    }
                    entity.playerStandTimer = 0;
                }
            } else {
                entity.playerStandTimer = 0;
            }
        }
    }
}