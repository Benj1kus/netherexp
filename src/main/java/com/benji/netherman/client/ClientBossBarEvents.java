package com.benji.netherman.client;

import com.benji.netherman.NetherExp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetherExp.MODID, value = Dist.CLIENT)
public class ClientBossBarEvents {
    private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_frame.png");
    private static final ResourceLocation PROGRESS_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_progress.png");
    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_frame_sun.png"); 
    private static final ResourceLocation CINEMATIC_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/cinematic.png");
    private static final ResourceLocation SUN_LOWHP_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_frame_sun_lowhp.png");

    private static final ResourceLocation HUMAN_SUN_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_human_sun.png");
    private static final ResourceLocation HUMAN_SUN_LOWHP_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_human_sun_lowhp.png");

    @SubscribeEvent
    public static void onRenderMercyText(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        com.benji.netherman.entity.AzazelEntity azazel = null;
        for (Entity entity : mc.level.getEntitiesOfClass(com.benji.netherman.entity.AzazelEntity.class, mc.player.getBoundingBox().inflate(30.0D))) {
            int state = ((com.benji.netherman.entity.AzazelEntity) entity).getEntityData().get(com.benji.netherman.entity.AzazelEntity.ATTACK_STATE);
            if (state >= 6 && state <= 9) {
                azazel = (com.benji.netherman.entity.AzazelEntity) entity;
                break;
            }
        }

        if (azazel != null) {
            int state = azazel.getEntityData().get(com.benji.netherman.entity.AzazelEntity.ATTACK_STATE);
            GuiGraphics graphics = event.getGuiGraphics();
            int screenWidth = graphics.guiWidth();
            int screenHeight = graphics.guiHeight();
            if (state == 8 || state == 9) {
                graphics.blit(CINEMATIC_TEXTURE, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);
            }
            if (state == 8) return;

            int mercyTick = azazel.getEntityData().get(com.benji.netherman.entity.AzazelEntity.MERCY_TICK);
            String fullText = "";
            if (state == 6) fullText = I18n.get("entity.netherman.azazel.surrender");
            else if (state == 7) fullText = I18n.get("entity.netherman.azazel.mercy");
            else if (state == 9) fullText = I18n.get("entity.netherman.azazel.death");

            int charsToShow = Math.min(fullText.length(), mercyTick / 2);
            String textToRender = fullText.substring(0, charsToShow);

            int boxWidth = mc.font.width(fullText) + 20;
            int boxHeight = 24;
            int boxX = (screenWidth - boxWidth) / 2;
            int boxY = screenHeight - 120;

            graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0x99000000);
            graphics.drawString(mc.font, textToRender, boxX + 10, boxY + 8, 0xFFFF55, false);
        }
    }

    @SubscribeEvent
    public static void onRenderBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        Component name = event.getBossEvent().getName();
        if (name.getString().contains("Azazel")) {
            event.setCanceled(true);

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            com.benji.netherman.entity.AzazelEntity azazel = null;
            for (Entity entity : mc.level.getEntitiesOfClass(com.benji.netherman.entity.AzazelEntity.class, mc.player.getBoundingBox().inflate(100.0D))) {
                azazel = (com.benji.netherman.entity.AzazelEntity) entity;
                break;
            }
            
            com.benji.netherman.entity.AzazelHumanEntity azazelHuman = null;
            for (Entity entity : mc.level.getEntitiesOfClass(com.benji.netherman.entity.AzazelHumanEntity.class, mc.player.getBoundingBox().inflate(100.0D))) {
                azazelHuman = (com.benji.netherman.entity.AzazelHumanEntity) entity;
                break;
            }

            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenWidth = guiGraphics.guiWidth();
            int y = event.getY();
            int frameWidth = 186;
            int frameHeight = 42;
            int frameX = (screenWidth / 2) - (frameWidth / 2);
            int frameY = y;
            
            ResourceLocation frameToUse = (azazelHuman != null) ?
                    new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_human_frame.png") : FRAME_TEXTURE;

            if (azazelHuman != null && (azazelHuman.getHealth() / azazelHuman.getMaxHealth()) < 0.3F) {
                frameToUse = new ResourceLocation(NetherExp.MODID, "textures/gui/azazel_human_frame_lowhp.png");
            }

            guiGraphics.blit(frameToUse, frameX, frameY, 0, 0, frameWidth, frameHeight, frameWidth, frameHeight);

            float progress = event.getBossEvent().getProgress();
            int progressMaxWidth = 182;
            int progressHeight = 5;
            int currentProgressWidth = (int) (progressMaxWidth * progress);

            if (currentProgressWidth > 0) {
                guiGraphics.blit(PROGRESS_TEXTURE, frameX + 8, frameY + 18, 0, 0, currentProgressWidth, progressHeight, progressMaxWidth, progressHeight);
            }

            ResourceLocation currentSunTexture = null;

            if (azazel != null) {
                currentSunTexture = SUN_TEXTURE;
                if (azazel.getEntityData().get(com.benji.netherman.entity.AzazelEntity.PHASE_STATE) == 2) {
                    currentSunTexture = SUN_LOWHP_TEXTURE;
                }
            } else if (azazelHuman != null) {
                currentSunTexture = HUMAN_SUN_TEXTURE;
                if ((azazelHuman.getHealth() / azazelHuman.getMaxHealth()) < 0.3F) {
                    currentSunTexture = HUMAN_SUN_LOWHP_TEXTURE;
                }
            }

            if (currentSunTexture != null) {
                guiGraphics.blit(currentSunTexture, frameX, frameY, 0, 0, frameWidth, frameHeight, frameWidth, frameHeight);
            }

            event.setIncrement(frameHeight + 5);
        }
    }
}