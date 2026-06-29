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
    private static int bossMusicTimer = 0;
    private static boolean isPlayingBossIntro = false;
    private static int shakeTimer = 0;
    private static float shakeIntensity = 0.0F;

    public static void startScreenShake(int ticks, float intensity) {
        shakeTimer = ticks;
        shakeIntensity = intensity;
    }

    @SubscribeEvent
    public static void onCameraSetup(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles event) {
        if (shakeTimer > 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.isPaused()) return;

            shakeTimer--;

            
            float shakeX = (mc.player.getRandom().nextFloat() - 0.5F) * shakeIntensity;
            float shakeY = (mc.player.getRandom().nextFloat() - 0.5F) * shakeIntensity;
            float shakeZ = (mc.player.getRandom().nextFloat() - 0.5F) * shakeIntensity;

            event.setPitch(event.getPitch() + shakeX);
            event.setYaw(event.getYaw() + shakeY);
            event.setRoll(event.getRoll() + shakeZ);

            
            if (shakeTimer < 10) {
                shakeIntensity *= 0.8F;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player != Minecraft.getInstance().player) return;
        LocalPlayer player = (LocalPlayer) event.player;


        if (Minecraft.getInstance().screen == null) {
            for (com.benji.netherman.entity.AzazelHumanEntity boss : player.level().getEntitiesOfClass(com.benji.netherman.entity.AzazelHumanEntity.class, player.getBoundingBox().inflate(20.0D))) {
                int state = boss.getEntityData().get(com.benji.netherman.entity.AzazelHumanEntity.BOSS_STATE);
                if (state == 2 || state == 3 || state == 100) {
                    Minecraft.getInstance().setScreen(new com.benji.netherman.client.gui.AzazelHumanCutsceneScreen(boss));
                    break;
                }
            }
        }

        int currentZoneType = -1;
        MobEffect activeEffect = null;

        if (player.hasEffect(NetherExp.ALERTNESS_EFFECT.get())) {
            currentZoneType = 4;
            activeEffect = NetherExp.ALERTNESS_EFFECT.get();
        } else if (player.hasEffect(NetherExp.ANXIETY_EFFECT.get())) {
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

        if (currentZoneType == 3 && isPlayingBossIntro) {
            bossMusicTimer--;
            if (bossMusicTimer <= 0) {
                isPlayingBossIntro = false;
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
                    currentAmbientSound = new ZoneAmbientSoundInstance(ModSounds.BOSS_FIGHT.get(), player, activeEffect, false);
                    Minecraft.getInstance().getSoundManager().play(currentAmbientSound);

                    bossMusicTimer = 2900;
                    isPlayingBossIntro = true;
                } else {
                    var soundEvent = switch (currentZoneType) {
                        case 4 -> ModSounds.MAZE_AMBIENT.get();
                        case 2 -> ModSounds.CHURCH_AMBIENT.get();
                        case 1 -> ModSounds.CITY_AMBIENT.get();
                        default -> ModSounds.CAVE_AMBIENT.get();
                    };
                    isPlayingBossIntro = false;
                    currentAmbientSound = new ZoneAmbientSoundInstance(soundEvent, player, activeEffect, true);
                    Minecraft.getInstance().getSoundManager().play(currentAmbientSound);
                }
            } else {
                isPlayingBossIntro = false;
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