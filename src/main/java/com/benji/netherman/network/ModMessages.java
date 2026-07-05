package com.benji.netherman.network;

import com.benji.netherman.NetherExp;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(NetherExp.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.registerMessage(id(), AzazelBoostPacket.class,
                AzazelBoostPacket::toBytes,
                AzazelBoostPacket::new,
                AzazelBoostPacket::handle);

        net.messageBuilder(FogSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FogSyncS2CPacket::new)
                .encoder(FogSyncS2CPacket::toBytes)
                .consumerMainThread(FogSyncS2CPacket::handle)
                .add();

        net.messageBuilder(TotemAnimationPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TotemAnimationPacket::new)
                .encoder(TotemAnimationPacket::toBytes)
                .consumerMainThread(TotemAnimationPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}