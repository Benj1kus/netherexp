package com.benji.netherman.network;

import com.benji.netherman.NetherExp;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AzazelBoostPacket {
    public AzazelBoostPacket() {
    }

    public AzazelBoostPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.isFallFlying()) {
                ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestStack.is(NetherExp.AZAZEL_CHESTPLATE.get())) {
                    chestStack.hurtAndBreak(15, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.CHEST));
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}