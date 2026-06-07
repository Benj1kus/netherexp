package com.benji.netherman.event;

import com.benji.netherman.network.FogSyncS2CPacket;
import com.benji.netherman.network.ModMessages;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "netherman", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MansionCheckHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!event.player.level().isClientSide() && event.phase == TickEvent.Phase.END && event.player.tickCount % 20 == 0) {
            ServerLevel level = (ServerLevel) event.player.level();
            ServerPlayer serverPlayer = (ServerPlayer) event.player; // Приводим к серверному игроку

            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            Structure mansion = registry.get(new ResourceLocation("netherman", "mansion_nether"));

            boolean isInside = false;

            if (mansion != null) {
                StructureStart start = level.structureManager().getStructureAt(event.player.blockPosition(), mansion);
                isInside = start.isValid();
            }

            // Отправляем пакет клиенту!
            ModMessages.sendToPlayer(new FogSyncS2CPacket(isInside), serverPlayer);
        }
    }
}