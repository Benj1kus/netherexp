package com.benji.netherman.client;

import com.benji.netherman.NetherExp;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetherExp.MODID, value = Dist.CLIENT)
public class ClientBossBarEvents {

    // Пути к текстурам босс-бара
    private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_frame.png");
    private static final ResourceLocation PROGRESS_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_progress.png");
    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_frame_sun.png"); // Новая вставка

    @SubscribeEvent
    public static void onRenderBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        Component name = event.getBossEvent().getName();

        // Проверяем, что сейчас рисуется именно босс-бар Азазеля
        if (name.getString().contains("Azazel")) {

            // Отменяем ванильную отрисовку
            event.setCanceled(true);

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenWidth = guiGraphics.guiWidth();
            int y = event.getY();

            // 1. РАЗМЕРЫ И ПОЗИЦИЯ ОСНОВНОЙ РАМКИ
            int frameWidth = 186;
            int frameHeight = 42;
            int frameX = (screenWidth / 2) - (frameWidth / 2);
            int frameY = y;

            // СЛОЙ 1: Отрисовываем задний фон рамки
            guiGraphics.blit(FRAME_TEXTURE, frameX, frameY, 0, 0, frameWidth, frameHeight, frameWidth, frameHeight);

            // 2. РАЗМЕРЫ И ПОЗИЦИЯ ПОЛОСКИ КРОВИ
            float progress = event.getBossEvent().getProgress();
            int progressMaxWidth = 182;
            int progressHeight = 5;

            int currentProgressWidth = (int) (progressMaxWidth * progress);
            int progressX = frameX + 8; // Сдвинет полоску крови еще на 3 пикселя правее
            int progressOffsetY = 18;
            int progressY = frameY + progressOffsetY;

            // СЛОЙ 2: Рисуем заполнение ХП (поверх задней рамки)
            if (currentProgressWidth > 0) {
                guiGraphics.blit(PROGRESS_TEXTURE, progressX, progressY, 0, 0, currentProgressWidth, progressHeight, progressMaxWidth, progressHeight);
            }

            // СЛОЙ 3: Отрисовка вставки-лица (поверх крови и рамки)
            // Размеры и координаты идентичны базовой рамке
            guiGraphics.blit(SUN_TEXTURE, frameX, frameY, 0, 0, frameWidth, frameHeight, frameWidth, frameHeight);

            // Сдвигаем Y-координату для других босс-баров (если они появятся)
            event.setIncrement(frameHeight + 5);
        }
    }
}