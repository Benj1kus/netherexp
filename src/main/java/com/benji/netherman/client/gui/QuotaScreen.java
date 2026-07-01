package com.benji.netherman.client.gui;

import com.benji.netherman.NetherExp;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class QuotaScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/quota_gui.png");

    private static final String[] TASK_TEXTURES = {
            "quota_altar_line", "quota_destroy_line", "quota_kill_line",
            "quota_mine_line", "quota_pillagers_line", "quota_villagers_line"
    };

    private static final int IMAGE_WIDTH = 260;
    private static final int IMAGE_HEIGHT = 160;

    private long timeOpened;
    private static final float ANIMATION_DURATION_MS = 400.0f;
    private final ItemStack quotaItem;

    public QuotaScreen(ItemStack stack) {
        super(Component.empty());
        this.quotaItem = stack;
    }

    @Override
    protected void init() {
        super.init();
        this.timeOpened = System.currentTimeMillis();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        long timePassed = System.currentTimeMillis() - this.timeOpened;
        float progress = Math.min(1.0f, timePassed / ANIMATION_DURATION_MS);
        float easeOutProgress = 1.0f - (1.0f - progress) * (1.0f - progress);

        int x = (this.width - IMAGE_WIDTH) / 2;
        int startY = this.height;
        int endY = (this.height - IMAGE_HEIGHT) / 2;
        int currentY = (int) (startY + (endY - startY) * easeOutProgress);

        guiGraphics.blit(BG_TEXTURE, x, currentY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

        if (this.quotaItem.hasTag() && this.quotaItem.getTag().contains("QuotaData")) {
            CompoundTag data = this.quotaItem.getTag().getCompound("QuotaData");

            int stage = data.getInt("QuotaStage");
            String stageTexture = stage >= 6 ? "quota_stage_last.png" : "quota_stage_" + stage + ".png";
            guiGraphics.blit(new ResourceLocation(NetherExp.MODID, "textures/gui/" + stageTexture), x, currentY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

            for (int i = 1; i <= 3; i++) {
                if (data.contains("QuotaTask" + i)) {
                    int taskType = data.getInt("QuotaTask" + i);
                    int prog = data.getInt("QuotaProg" + i);
                    int max = data.getInt("QuotaMax" + i);

                    if (prog >= max) continue;

                    ResourceLocation taskTex = new ResourceLocation(NetherExp.MODID, "textures/gui/" + TASK_TEXTURES[taskType] + i + ".png");
                    guiGraphics.blit(taskTex, x, currentY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

                    int lineStartY = currentY + 30 + (i - 1) * 35;
                    int lineEndY = lineStartY + 30;

                    if (mouseX >= x + 20 && mouseX <= x + 240 && mouseY >= lineStartY && mouseY <= lineEndY) {
                        ResourceLocation frameTex = new ResourceLocation(NetherExp.MODID, "textures/gui/quota_selected_line" + i + ".png");
                        guiGraphics.blit(frameTex, x, currentY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

                        renderTooltipText(guiGraphics, taskType, prog, max, mouseX, mouseY);
                    }
                }
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private static final ResourceLocation ALTAR_HINT_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/altar_hint.png");

    private void renderTooltipText(GuiGraphics guiGraphics, int taskType, int prog, int max, int mouseX, int mouseY) {
        Component text;
        switch (taskType) {
            case 0:
                text = Component.translatable("gui.netherman.quota.altar");

                int hintWidth = 64;
                int hintHeight = 64;
                int hintX = mouseX - hintWidth - 10;
                int hintY = mouseY - hintHeight / 2;
                guiGraphics.blit(ALTAR_HINT_TEXTURE, hintX, hintY, 0, 0, hintWidth, hintHeight, hintWidth, hintHeight);
                break;
            case 1: text = Component.translatable("gui.netherman.quota.destroy", prog, max); break;
            case 2: text = Component.translatable("gui.netherman.quota.sacrifice", prog, max); break;
            case 3: text = Component.translatable("gui.netherman.quota.mine", prog, max); break;
            case 4: text = Component.translatable("gui.netherman.quota.pillagers", prog, max); break;
            case 5: text = Component.translatable("gui.netherman.quota.villagers", prog, max); break;
            default: text = Component.empty();
        }

        guiGraphics.renderTooltip(this.font, text, mouseX + 15, mouseY - 15);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}