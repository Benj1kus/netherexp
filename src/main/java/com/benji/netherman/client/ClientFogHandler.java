package com.benji.netherman.client;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "netherman", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientFogHandler {

    public static boolean isInsideMansion = false;
    private static float fogProgress = 0.0f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (isInsideMansion && fogProgress < 1.0f) {
                fogProgress += 0.01f;
            } else if (!isInsideMansion && fogProgress > 0.0f) {
                fogProgress -= 0.01f;
            }
            fogProgress = Mth.clamp(fogProgress, 0.0f, 1.0f);
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (fogProgress > 0.0f) {
            float targetRed = 0.6f;
            float targetGreen = 0.0f;
            float targetBlue = 0.02f;

            event.setRed(Mth.lerp(fogProgress, event.getRed(), targetRed));
            event.setGreen(Mth.lerp(fogProgress, event.getGreen(), targetGreen));
            event.setBlue(Mth.lerp(fogProgress, event.getBlue(), targetBlue));
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (fogProgress > 0.0f) {

            float targetFarPlane = 48.0f; // Дистанция, где туман становится 100% непроглядным (попробуй 48 или 64)
            float targetNearPlane = 12.0f; // Дистанция от игрока, где туман ТОЛЬКО НАЧИНАЕТ появляться

            float currentFar = event.getFarPlaneDistance();
            float currentNear = event.getNearPlaneDistance();

            event.setNearPlaneDistance(
                    Mth.lerp(fogProgress, currentNear, targetNearPlane));

            event.setFarPlaneDistance(
                    Mth.lerp(fogProgress, currentFar, targetFarPlane));

            event.setCanceled(true);
        }
    }
}