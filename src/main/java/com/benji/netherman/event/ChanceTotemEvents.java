package com.benji.netherman.event;

import com.benji.netherman.NetherExp;
import com.benji.netherman.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = NetherExp.MODID)
public class ChanceTotemEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            ItemStack totem = null;

            // Проверяем, есть ли Тотем Шансов в любой из рук
            if (mainHand.is(NetherExp.CHANCE_TOTEM.get())) {
                totem = mainHand;
            } else if (offHand.is(NetherExp.CHANCE_TOTEM.get())) {
                totem = offHand;
            }

            if (totem != null) {
                // 1. Отменяем смерть и восстанавливаем 1 ХП
                event.setCanceled(true);
                player.setHealth(1.0F);
                player.removeAllEffects(); // Снимаем старые эффекты (яд, иссушение и т.д.)

                // 2. Тратим тотем
                totem.shrink(1);

                ServerLevel currentLevel = (ServerLevel) player.level();

                // 3. Звук и партиклы на месте "смерти"
                currentLevel.playSound(null, player.blockPosition(), ModSounds.RESPAWN_TOTEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                currentLevel.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.5D, 0.5D, 0.5D, 0.05D);

                // 4. Поиск точки спавна (кровать, якорь или спавн мира)
                ServerLevel respawnLevel = currentLevel.getServer().getLevel(player.getRespawnDimension());
                if (respawnLevel == null) respawnLevel = currentLevel.getServer().overworld();

                BlockPos respawnPos = player.getRespawnPosition();
                float respawnAngle = player.getRespawnAngle();

                if (respawnPos != null) {
                    // Пытаемся найти безопасную точку около кровати/якоря
                    Optional<Vec3> safePos = Player.findRespawnPositionAndUseSpawnBlock(respawnLevel, respawnPos, respawnAngle, player.isRespawnForced(), true);
                    if (safePos.isPresent()) {
                        player.teleportTo(respawnLevel, safePos.get().x, safePos.get().y, safePos.get().z, respawnAngle, 0.0F);
                    } else {
                        // Кровать сломана - кидаем на спавн мира
                        BlockPos sharedSpawn = respawnLevel.getSharedSpawnPos();
                        player.teleportTo(respawnLevel, sharedSpawn.getX(), sharedSpawn.getY(), sharedSpawn.getZ(), respawnAngle, 0.0F);
                    }
                } else {
                    // Точка спавна не задана - кидаем на спавн мира
                    BlockPos sharedSpawn = respawnLevel.getSharedSpawnPos();
                    player.teleportTo(respawnLevel, sharedSpawn.getX(), sharedSpawn.getY(), sharedSpawn.getZ(), respawnAngle, 0.0F);
                }

                // 5. Накладываем пост-эффекты на 30 секунд (600 тиков). Уровни (amplifier) начинаются с 0.
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 4)); // Сопротивление 5
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0));      // Невидимость 1
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600, 1));         // Слепота 2
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 2));          // Слабость 3
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 4));      // Регенерация 5
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 0)); // Замедление 1

                // 6. Звук и партиклы на новой позиции (чтобы игрок понял, что переместился с эффектом)
                respawnLevel.playSound(null, player.blockPosition(), ModSounds.RESPAWN_TOTEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                respawnLevel.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.5D, 0.5D, 0.5D, 0.05D);
            }
        }
    }
}