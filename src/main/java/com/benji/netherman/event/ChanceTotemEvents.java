package com.benji.netherman.event;

import com.benji.netherman.NetherExp;
import com.benji.netherman.ModSounds;
import com.benji.netherman.network.ModMessages;
import com.benji.netherman.network.TotemAnimationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = NetherExp.MODID)
public class ChanceTotemEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            ItemStack totem = null;
            if (mainHand.is(NetherExp.CHANCE_TOTEM.get())) {
                totem = mainHand;
            } else if (offHand.is(NetherExp.CHANCE_TOTEM.get())) {
                totem = offHand;
            }

            if (totem != null) {
                event.setCanceled(true);
                player.setHealth(1.0F);
                player.removeAllEffects();
                totem.shrink(1);

                ModMessages.sendToPlayer(new TotemAnimationPacket(), player);

                ServerLevel currentLevel = (ServerLevel) player.level();
                currentLevel.playSound(null, player.blockPosition(), ModSounds.RESPAWN_TOTEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                currentLevel.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.5D, 0.5D, 0.5D, 0.05D);

                ServerLevel respawnLevel = currentLevel.getServer().getLevel(player.getRespawnDimension());
                if (respawnLevel == null) respawnLevel = currentLevel.getServer().overworld();

                BlockPos respawnPos = player.getRespawnPosition();
                float respawnAngle = player.getRespawnAngle();

                if (respawnPos != null) {
                    Optional<Vec3> safePos = Player.findRespawnPositionAndUseSpawnBlock(respawnLevel, respawnPos, respawnAngle, player.isRespawnForced(), true);
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

                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 4));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 4));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 0));

                respawnLevel.playSound(null, player.blockPosition(), ModSounds.RESPAWN_TOTEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                respawnLevel.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.5D, 0.5D, 0.5D, 0.05D);
            }
        }
    }
}