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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player != Minecraft.getInstance().player) return;
        LocalPlayer player = (LocalPlayer) event.player;

        int currentZoneType = -1;
        MobEffect activeEffect = null;

        if (player.hasEffect(NetherExp.FAITH_EFFECT.get())) {
            currentZoneType = 2;
            activeEffect = NetherExp.FAITH_EFFECT.get();
        } else if (player.hasEffect(NetherExp.EXCITEMENT_EFFECT.get())) {
            currentZoneType = 1;
            activeEffect = NetherExp.EXCITEMENT_EFFECT.get();
        } else if (player.hasEffect(NetherExp.FEAR_EFFECT.get())) {
            currentZoneType = 0;
            activeEffect = NetherExp.FEAR_EFFECT.get();
        }

        // Если зона сменилась или эффект полностью пропал
        if (currentZoneType != lastZoneType) {

            // Если какой-то эмбиент уже играл - принудительно его глушим
            if (currentAmbientSound != null) {
                Minecraft.getInstance().getSoundManager().stop(currentAmbientSound);
                currentAmbientSound = null;
            }

            // Запускаем новый эмбиент
            if (currentZoneType != -1) {
                var soundEvent = switch (currentZoneType) {
                    case 2 -> ModSounds.CHURCH_AMBIENT.get();
                    case 1 -> ModSounds.CITY_AMBIENT.get();
                    default -> ModSounds.CAVE_AMBIENT.get();
                };

                // Создаем наш умный звук и отправляем его в SoundManager
                currentAmbientSound = new ZoneAmbientSoundInstance(soundEvent, player, activeEffect);
                Minecraft.getInstance().getSoundManager().play(currentAmbientSound);
            }

            lastZoneType = currentZoneType;
        }

        // Сброс `lastZoneType`, если звук остановился сам (например, игрок выпил молоко)
        // Чтобы при новом получении эффекта звук включился снова
        if (currentZoneType == -1 && lastZoneType != -1) {
            lastZoneType = -1;
            currentAmbientSound = null;
        }
    }
}