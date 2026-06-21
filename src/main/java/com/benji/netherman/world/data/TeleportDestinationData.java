package com.benji.netherman.world.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class TeleportDestinationData extends SavedData {
    private final Set<BlockPos> destinations = new HashSet<>();

    public void addDestination(BlockPos pos) {
        if (destinations.add(pos)) {
            this.setDirty();
        }
    }

    public void removeDestination(BlockPos pos) {
        if (destinations.remove(pos)) {
            this.setDirty();
        }
    }

    public BlockPos getRandomDestination(net.minecraft.util.RandomSource random) {
        if (destinations.isEmpty()) return null;
        int index = random.nextInt(destinations.size());
        int i = 0;
        for (BlockPos pos : destinations) {
            if (i == index) return pos;
            i++;
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (BlockPos pos : destinations) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("Destinations", list);
        return tag;
    }

    public static TeleportDestinationData load(CompoundTag tag) {
        TeleportDestinationData data = new TeleportDestinationData();
        ListTag list = tag.getList("Destinations", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            data.destinations.add(NbtUtils.readBlockPos(list.getCompound(i)));
        }
        return data;
    }

    public static TeleportDestinationData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                TeleportDestinationData::load,
                TeleportDestinationData::new,
                "netherman_teleports"
        );
    }
}