package com.benji.netherman.network;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelHumanEntity;
import com.benji.netherman.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Optional;

public class ServerPayloadHandler {

    public static void handleAzazelCutscene(AzazelCutscenePacket payload, ServerPlayer player) {
        Entity target = player.level().getEntity(payload.getEntityId());

        if (target instanceof AzazelHumanEntity boss && boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE) == 3) {

            if (payload.getActionId() == 0) {
                ServerLevel currentLevel = (ServerLevel) boss.level();
                ServerLevel respawnLevel = currentLevel.getServer().getLevel(player.getRespawnDimension());
                if (respawnLevel == null) respawnLevel = currentLevel.getServer().overworld();

                BlockPos respawnPos = player.getRespawnPosition();
                float respawnAngle = player.getRespawnAngle();

                if (respawnPos != null) {
                    Optional<Vec3> safePos = net.minecraft.world.entity.player.Player.findRespawnPositionAndUseSpawnBlock(
                            respawnLevel, respawnPos, respawnAngle, player.isRespawnForced(), true);

                    if (safePos.isPresent()) {
                        player.teleportTo(respawnLevel, safePos.get().x, safePos.get().y, safePos.get().z, respawnAngle, 0.0F);
                    } else {
                        BlockPos sharedSpawn = respawnLevel.getSharedSpawnPos();
                        player.teleportTo(respawnLevel, sharedSpawn.getX(), sharedSpawn.getY(), sharedSpawn.getZ(), respawnAngle, 0.0F);
                    }
                } else {
                    BlockPos sharedSpawn = respawnLevel.getSharedSpawnPos();
                    player.teleportTo(respawnLevel, sharedSpawn.getX(), sharedSpawn.getY(), sharedSpawn.getZ(), respawnAngle, 0.0F);
                }

                respawnLevel.playSound(null, player.blockPosition(), ModSounds.BELL_BEAST_LAUGH.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
                DustParticleOptions redMagic = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
                respawnLevel.sendParticles(player, redMagic, true, player.getX(), player.getY() + 1.0D, player.getZ(), 80, 0.8D, 1.0D, 0.8D, 0.1D);

                BlockPos barrelPos = player.blockPosition();
                for (int[] offset : new int[][]{{1,0,0}, {-1,0,0}, {0,0,1}, {0,0,-1}, {0,1,0}}) {
                    BlockPos checkPos = player.blockPosition().offset(offset[0], offset[1], offset[2]);
                    if (respawnLevel.getBlockState(checkPos).canBeReplaced()) {
                        barrelPos = checkPos;
                        break;
                    }
                }

                respawnLevel.setBlockAndUpdate(barrelPos, Blocks.BARREL.defaultBlockState());
                respawnLevel.playSound(null, barrelPos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0F, 1.5F);
                respawnLevel.sendParticles(player, ParticleTypes.TOTEM_OF_UNDYING, true, barrelPos.getX() + 0.5D, barrelPos.getY() + 1.0D, barrelPos.getZ() + 0.5D, 60, 0.4D, 0.5D, 0.4D, 0.2D);

                net.minecraft.world.level.block.entity.BlockEntity blockEntity = respawnLevel.getBlockEntity(barrelPos);
                if (blockEntity instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity barrel) {
                    barrel.setItem(0, new ItemStack(NetherExp.AZAZEL_HELMET.get(), 1));
                    barrel.setItem(1, new ItemStack(NetherExp.AZAZEL_CHESTPLATE.get(), 1));
                    barrel.setItem(2, new ItemStack(NetherExp.AZAZEL_LEGGINGS.get(), 1));
                    barrel.setItem(3, new ItemStack(NetherExp.AZAZEL_BOOTS.get(), 1));
                    barrel.setItem(4, new ItemStack(NetherExp.QUOTA.get(), 1));
                    barrel.setItem(5, new ItemStack(NetherExp.FAITH_PART.get(), 5));
                    barrel.setItem(6, new ItemStack(NetherExp.FAITH_ESSENCE.get(), 1));
                }

                player.getPersistentData().putBoolean("AzazelCultist", true);
                com.benji.netherman.QuotaManager.generateNewQuota(player);

                boss.discard();

            } else if (payload.getActionId() == 1) {
                boss.getEntityData().set(AzazelHumanEntity.BOSS_STATE, 4);
                boss.getEntityData().set(AzazelHumanEntity.DIALOGUE_TICK, 0);
                boss.playSound(ModSounds.LAUGH.get(), 2.0F, 1.0F);

                Vec3 forward = Vec3.directionFromRotation(0, boss.getYRot()).normalize();
                boss.setPos(boss.getX() + forward.x * 2.0D, boss.getY(), boss.getZ() + forward.z * 2.0D);

                if (boss.level() instanceof ServerLevel sl) {
                    for (ServerPlayer p : sl.getEntitiesOfClass(ServerPlayer.class, boss.getBoundingBox().inflate(64.0D))) {
                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(NetherExp.PRAEMIUM.get(), -1, 0, false, false, true));
                    }
                }
            }
        }
    }
}