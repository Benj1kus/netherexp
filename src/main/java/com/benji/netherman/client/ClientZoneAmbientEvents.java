package com.benji.netherman.client;

import com.benji.netherman.NetherExp;
import com.benji.netherman.ModSounds;
import com.benji.netherman.client.sound.ZoneAmbientSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetherExp.MODID, value = Dist.CLIENT)
public class ClientZoneAmbientEvents {

    private static ZoneAmbientSoundInstance currentAmbientSound = null;
    private static int lastZoneType = -1;

    // Таймеры для интро босса
    private static int bossMusicTimer = 0;
    private static boolean isPlayingBossIntro = false;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player != Minecraft.getInstance().player) return;
        LocalPlayer player = (LocalPlayer) event.player;

        int currentZoneType = -1;
        MobEffect activeEffect = null;

        // ВАЖНО: Тревога от Босса имеет высший приоритет и перекрывает остальные зоны!
        if (player.hasEffect(NetherExp.ANXIETY_EFFECT.get())) {
            currentZoneType = 3;
            activeEffect = NetherExp.ANXIETY_EFFECT.get();
        } else if (player.hasEffect(NetherExp.FAITH_EFFECT.get())) {
            currentZoneType = 2;
            activeEffect = NetherExp.FAITH_EFFECT.get();
        } else if (player.hasEffect(NetherExp.EXCITEMENT_EFFECT.get())) {
            currentZoneType = 1;
            activeEffect = NetherExp.EXCITEMENT_EFFECT.get();
        } else if (player.hasEffect(NetherExp.FEAR_EFFECT.get())) {
            currentZoneType = 0;
            activeEffect = NetherExp.FEAR_EFFECT.get();
        }

        // Логика перехода от Интро Босса к Лупу
        if (currentZoneType == 3 && isPlayingBossIntro) {
            bossMusicTimer--;
            if (bossMusicTimer <= 0) {
                isPlayingBossIntro = false;

                // Глушим интро (на всякий случай) и запускаем зацикленный луп
                if (currentAmbientSound != null) {
                    Minecraft.getInstance().getSoundManager().stop(currentAmbientSound);
                }

                currentAmbientSound = new ZoneAmbientSoundInstance(ModSounds.BOSS_FIGHT_LOOP.get(), player, activeEffect, true);
                Minecraft.getInstance().getSoundManager().play(currentAmbientSound);
            }
        }

        if (currentZoneType != lastZoneType) {
            if (currentAmbientSound != null) {
                Minecraft.getInstance().getSoundManager().stop(currentAmbientSound);
                currentAmbientSound = null;
            }

            if (currentZoneType != -1) {
                if (currentZoneType == 3) {
                    // НАЧАЛО БОЯ С БОССОМ
                    currentAmbientSound = new ZoneAmbientSoundInstance(ModSounds.BOSS_FIGHT.get(), player, activeEffect, false); // false = не зацикливать интро!
                    Minecraft.getInstance().getSoundManager().play(currentAmbientSound);

                    bossMusicTimer = 6020; // 2 мин 25 сек (145 сек * 20 тиков)
                    isPlayingBossIntro = true;
                } else {
                    // ОБЫЧНЫЕ ЗОНЫ
                    var soundEvent = switch (currentZoneType) {
                        case 2 -> ModSounds.CHURCH_AMBIENT.get();
                        case 1 -> ModSounds.CITY_AMBIENT.get();
                        default -> ModSounds.CAVE_AMBIENT.get();
                    };
                    isPlayingBossIntro = false;
                    currentAmbientSound = new ZoneAmbientSoundInstance(soundEvent, player, activeEffect, true); // true = зациклить
                    Minecraft.getInstance().getSoundManager().play(currentAmbientSound);
                }
            } else {
                isPlayingBossIntro = false; // Эффекты пропали
            }

            lastZoneType = currentZoneType;
        }

        if (currentZoneType == -1 && lastZoneType != -1) {
            lastZoneType = -1;
            currentAmbientSound = null;
            isPlayingBossIntro = false;
        }
    }
}