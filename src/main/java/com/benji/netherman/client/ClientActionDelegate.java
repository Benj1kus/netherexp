package com.benji.netherman.client;

import net.minecraft.world.item.ItemStack;

public class ClientActionDelegate {

    public static void openNoteScreen() {
        com.benji.netherman.client.gui.NoteScreen.openScreen();
    }

    public static void openQuotaScreen(ItemStack stack) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new com.benji.netherman.client.gui.QuotaScreen(stack));
    }
}