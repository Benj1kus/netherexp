package com.benji.netherman.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TotemAnimationPacket {

    private final ItemStack stack;

    public TotemAnimationPacket(ItemStack stack) {
        this.stack = stack;
    }

    public TotemAnimationPacket(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientPacketHandler.handleCustomTotemAnimation(this.stack);
        });
        context.setPacketHandled(true);
    }
}