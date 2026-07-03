package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.AltarBlock;
import com.benji.netherman.block.LabyrinthTeleportBlock;
import com.benji.netherman.world.data.TeleportDestinationData;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LabyrinthTeleportBlockEntity extends BlockEntity {

    private int playerStandTimer = 0;

    public LabyrinthTeleportBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.LABYRINTH_TELEPORT_BE.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0.0D, 256.0D, 0.0D);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            if (state.hasProperty(LabyrinthTeleportBlock.MODE) && state.getValue(LabyrinthTeleportBlock.MODE) == 2) {
                TeleportDestinationData.get((ServerLevel) this.level).addDestination(this.worldPosition);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LabyrinthTeleportBlockEntity entity) {
        int currentMode = state.getValue(LabyrinthTeleportBlock.MODE);

        if (!level.isClientSide() && level.getGameTime() % 20 == 0) {
            int totalAltars = 0;
            int guessedAltars = 0;
            int radius = 5;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
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
                if (newMode == 2) {
                    TeleportDestinationData.get((ServerLevel) level).addDestination(pos);
                }
            }
        }

        if (!level.isClientSide() && currentMode == 1) {
            AABB detectionBox = new AABB(pos).move(0, 1, 0);
            List<Player> players = level.getEntitiesOfClass(Player.class, detectionBox);

            if (!players.isEmpty()) {
                entity.playerStandTimer++;

                if (entity.playerStandTimer >= 20) {
                    Player p = players.get(0);
                    ServerLevel serverLevel = (ServerLevel) level;

                    BlockPos dest = TeleportDestinationData.get(serverLevel).getClosestDestination(pos);

                    if (dest == null) {
                        ResourceKey<Structure> mazeKey = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(NetherExp.MODID, "maze"));
                        var structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
                        var mazeHolder = structureRegistry.getHolder(mazeKey);

                        if (mazeHolder.isPresent()) {

                            Pair<BlockPos, Holder<Structure>> result = serverLevel.getChunkSource().getGenerator()
                                    .findNearestMapStructure(serverLevel, HolderSet.direct(mazeHolder.get()), pos, 100, false);

                            if (result != null) {
                                BlockPos structPos = result.getFirst();
                                int cx = structPos.getX() >> 4;
                                int cz = structPos.getZ() >> 4;
                                int radius = 2;

                                for (int x = -radius; x <= radius; x++) {
                                    for (int z = -radius; z <= radius; z++) {
                                        serverLevel.getChunk(cx + x, cz + z);
                                    }
                                }

                                dest = TeleportDestinationData.get(serverLevel).getClosestDestination(pos);
                            }
                        }
                    }

                    if (dest != null && !dest.equals(pos)) {
                        p.teleportTo(dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5);

                        level.destroyBlock(dest, false);

                        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.5F, 0.5F);
                        level.playSound(null, dest, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.5F, 0.8F);
                        serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, dest.getX() + 0.5, dest.getY() + 1.5, dest.getZ() + 0.5, 100, 0.5, 1.0, 0.5, 0.2);
                        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, dest.getX() + 0.5, dest.getY() + 1.0, dest.getZ() + 0.5, 50, 0.5, 0.2, 0.5, 0.1);
                    }
                    entity.playerStandTimer = 0;
                }
            } else {
                entity.playerStandTimer = 0;
            }
        }
    }
}