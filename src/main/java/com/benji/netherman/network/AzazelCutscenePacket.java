package com.benji.netherman.network;

import com.benji.netherman.entity.AzazelHumanEntity;
import com.benji.netherman.NetherExp;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AzazelCutscenePacket {
    private final int actionId;
    private final int entityId;

    public AzazelCutscenePacket(int actionId, int entityId) {
        this.actionId = actionId;
        this.entityId = entityId;
    }

    public AzazelCutscenePacket(FriendlyByteBuf buffer) {
        this.actionId = buffer.readInt();
        this.entityId = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.actionId);
        buffer.writeInt(this.entityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ServerPayloadHandler.handleAzazelCutscene(this, player);
            }
        });
        return true;
    }

    public int getActionId() { return actionId; }
    public int getEntityId() { return entityId; }
}