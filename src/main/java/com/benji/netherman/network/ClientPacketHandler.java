package com.benji.netherman.network;

import com.benji.netherman.NetherExp;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class ClientPacketHandler {
    public static void handleTotemAnimation() {
        Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(NetherExp.CHANCE_TOTEM.get()));
    }
}