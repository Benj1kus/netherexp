package com.benji.netherman.client.gui;

import com.benji.netherman.NetherExp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class NoteScreen extends Screen {
    private static final ResourceLocation NOTE_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/gui/note_gui.png");
    private static final int IMAGE_WIDTH = 130;
    private static final int IMAGE_HEIGHT = 160;

    private long timeOpened;
    private static final float ANIMATION_DURATION_MS = 400.0f;

    public NoteScreen() {
        super(Component.empty());
    }
    public static void openScreen() {
        Minecraft.getInstance().setScreen(new NoteScreen());
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
        guiGraphics.blit(NOTE_TEXTURE, x, currentY, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}