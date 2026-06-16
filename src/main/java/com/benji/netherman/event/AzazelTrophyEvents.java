package com.benji.netherman.event;

import com.benji.netherman.NetherExp;
import com.benji.netherman.ModSounds;
import com.benji.netherman.config.AzazelConfig;
import com.benji.netherman.network.ModMessages;
import com.benji.netherman.network.TotemAnimationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = NetherExp.MODID)
public class AzazelTrophyEvents {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);

            if (head.is(NetherExp.AZAZEL_TROPHY_ITEM.get()) && AzazelConfig.MASK_FIRE_IMMUNITY.get()) {
                DamageSource source = event.getSource();
                if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) ||
                        source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE) ||
                        source.is(net.minecraft.world.damagesource.DamageTypes.LAVA) ||
                        source.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {

                    event.setCanceled(true);
                }
            }
        }
    }

    // ВЫСШИЙ ПРИОРИТЕТ: Маска спасает игрока до того, как сработают любые тотемы в руках
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack mask = player.getItemBySlot(EquipmentSlot.HEAD);

            if (mask.is(NetherExp.AZAZEL_TROPHY_ITEM.get())) {
                CompoundTag tag = mask.getOrCreateTag();
                int stage = tag.getInt("TrophyStage");

                // Если есть внутренние заряды (1, 2 и 3 удары)
                if (stage < 3) {
                    event.setCanceled(true);

                    stage++;
                    tag.putInt("TrophyStage", stage);
                    tag.putInt("RegenTimer", 0);

                    player.setHealth(2.0F);
                    player.removeAllEffects();
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));

                    ServerLevel level = (ServerLevel) player.level();
                    ModMessages.sendToPlayer(new TotemAnimationPacket(), player);

                    level.playSound(null, player.blockPosition(), ModSounds.AZAZEL_DAMAGE_1.get(), SoundSource.PLAYERS, 1.2F, 1.0F);
                    level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 64, 0.3D, 0.3D, 0.3D, 0.5D);
                }
                // 4-й УДАР: Маска ломается и вызывает эффект CHANCE_TOTEM
                else if (stage == 3) {
                    event.setCanceled(true);

                    mask.shrink(1); // Уничтожаем маску
                    player.setHealth(1.0F);
                    player.removeAllEffects();

                    ServerLevel currentLevel = (ServerLevel) player.level();
                    currentLevel.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 1.0F, 0.5F);

                    triggerChanceTotemTeleport(player, currentLevel);
                }
            }
        }
    }

    private static void triggerChanceTotemTeleport(ServerPlayer player, ServerLevel currentLevel) {
        ModMessages.sendToPlayer(new TotemAnimationPacket(), player);
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
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 4));

        respawnLevel.playSound(null, player.blockPosition(), ModSounds.RESPAWN_TOTEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        respawnLevel.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.5D, 0.5D, 0.5D, 0.05D);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.player;
            ItemStack mask = player.getItemBySlot(EquipmentSlot.HEAD);

            if (mask.is(NetherExp.AZAZEL_TROPHY_ITEM.get())) {
                CompoundTag tag = mask.getOrCreateTag();
                int stage = tag.getInt("TrophyStage");

                if (stage > 0) {
                    int timer = tag.getInt("RegenTimer");
                    timer++;

                    if (timer >= AzazelConfig.MASK_REGEN_COOLDOWN.get()) {
                        timer = 0;
                        stage--;
                        tag.putInt("TrophyStage", stage);

                        ServerLevel level = (ServerLevel) player.level();
                        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);

                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                player.getX(), player.getY() + 1.7D, player.getZ(),
                                15, 0.25D, 0.25D, 0.25D, 0.05D);
                    }
                    tag.putInt("RegenTimer", timer);
                }
            }
        }
    }
}