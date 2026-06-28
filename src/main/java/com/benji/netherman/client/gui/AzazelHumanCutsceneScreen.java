package com.benji.netherman.client.gui;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelHumanEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.resources.language.I18n;

public class AzazelHumanCutsceneScreen extends Screen {
    private static final ResourceLocation CINEMATIC = new ResourceLocation(NetherExp.MODID, "textures/gui/cinematic_large.png");
    private static final ResourceLocation ICON_SURRENDER = new ResourceLocation(NetherExp.MODID, "textures/gui/surrender_icon.png");
    private static final ResourceLocation ICON_SURRENDER_HOVER = new ResourceLocation(NetherExp.MODID, "textures/gui/surrender_icon_choose.png");
    private static final ResourceLocation ICON_ATTACK = new ResourceLocation(NetherExp.MODID, "textures/gui/attack_icon.png");
    private static final ResourceLocation ICON_ATTACK_HOVER = new ResourceLocation(NetherExp.MODID, "textures/gui/attack_icon_choose.png");

    private final AzazelHumanEntity boss;

    public AzazelHumanCutsceneScreen(AzazelHumanEntity boss) {
        super(Component.literal("Azazel Cutscene"));
        this.boss = boss;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void tick() {
        super.tick();
        if (this.minecraft == null || this.minecraft.player == null) return;

        if (!boss.isAlive() || boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE) > 3) {
            this.minecraft.setScreen(null);
            return;
        }

        this.minecraft.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, boss.position().add(0, 12.0D, 0));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        graphics.blit(CINEMATIC, 0, 0, 0, 0, width, height, width, height);

        int state = boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE);
        int tick = boss.getEntityData().get(AzazelHumanEntity.DIALOGUE_TICK);
        //DIALOGUE
        if (state == 2) {
            int currentLine = tick / 90;
            if (currentLine > 9) currentLine = 9;

            int charsVisible = (tick % 90) / 2;

            String fullText = I18n.get("boss.netherman.azazel_human.line" + (currentLine + 1));
            String visibleText = fullText.substring(0, Math.min(charsVisible, fullText.length()));

            int textWidth = this.font.width(visibleText);
            graphics.drawString(this.font, visibleText, (width - textWidth) / 2, height - 40, 0xFFFFFF, true);
        }

        if (state == 3) {
            int btnSize = 22;
            int centerX = width / 2;
            int baseY = height - 60;

            int surrenderX = centerX - 40;
            int attackX = centerX + 40 - btnSize;

            boolean hoverSurrender = mouseX >= surrenderX && mouseX <= surrenderX + btnSize && mouseY >= baseY && mouseY <= baseY + btnSize;
            boolean hoverAttack = mouseX >= attackX && mouseX <= attackX + btnSize && mouseY >= baseY && mouseY <= baseY + btnSize;

            graphics.blit(hoverSurrender ? ICON_SURRENDER_HOVER : ICON_SURRENDER, surrenderX, baseY, 0, 0, btnSize, btnSize, btnSize, btnSize);
            graphics.blit(hoverAttack ? ICON_ATTACK_HOVER : ICON_ATTACK, attackX, baseY, 0, 0, btnSize, btnSize, btnSize, btnSize);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (boss.getEntityData().get(AzazelHumanEntity.BOSS_STATE) == 3 && button == 0) {
            int btnSize = 22;
            int centerX = this.width / 2;
            int baseY = this.height - 60;

            int surrenderX = centerX - 40;
            int attackX = centerX + 40 - btnSize;

            if (mouseX >= surrenderX && mouseX <= surrenderX + btnSize && mouseY >= baseY && mouseY <= baseY + btnSize) {
                this.minecraft.gameMode.interact(this.minecraft.player, boss, InteractionHand.MAIN_HAND);
                this.minecraft.setScreen(null);
                return true;
            }

            if (mouseX >= attackX && mouseX <= attackX + btnSize && mouseY >= baseY && mouseY <= baseY + btnSize) {
                this.minecraft.gameMode.attack(this.minecraft.player, boss);
                this.minecraft.setScreen(null);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}