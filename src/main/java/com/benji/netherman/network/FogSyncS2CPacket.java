package com.benji.netherman.network;

import com.benji.netherman.client.ClientFogHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FogSyncS2CPacket {
    private final boolean isInside;

    public FogSyncS2CPacket(boolean isInside) {
        this.isInside = isInside;
    }

    public FogSyncS2CPacket(FriendlyByteBuf buf) {
        this.isInside = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isInside);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Это код, который выполнится НА КЛИЕНТЕ, когда пакет придет
            ClientFogHandler.isInsideMansion = this.isInside;
        });
        return true;
    }
}