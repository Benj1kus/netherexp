package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.GhastlyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GhastlyNestBlockEntity extends BlockEntity {
    private final List<OccupantInfo> occupants = new ArrayList<>();
    private int honeyTimer = 0;
    private int spawnTimer = 0;

    public GhastlyNestBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.GHASTLY_NEST_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GhastlyNestBlockEntity nest) {
        if (nest.occupants.isEmpty()) return;

        Iterator<OccupantInfo> iterator = nest.occupants.iterator();
        while (iterator.hasNext()) {
            OccupantInfo info = iterator.next();
            info.ticksInHive++;

            if (info.ticksInHive > 1200 && level.random.nextInt(100) == 0) {
                nest.releaseGhastly(level, pos, info.entityData);
                iterator.remove();
            }
        }

        nest.spawnTimer++;
        if (nest.spawnTimer >= 6000) {
            nest.spawnTimer = 0;
            BlockPos spawnPos = null;

            for (int i = 0; i < 15; i++) {
                BlockPos p = pos.offset(level.random.nextInt(11) - 5, level.random.nextInt(5) - 2, level.random.nextInt(11) - 5);
                if (level.isEmptyBlock(p)) {
                    spawnPos = p;
                    break;
                }
            }

            if (spawnPos != null) {
                GhastlyEntity newGhastly = NetherExp.GHASTLY.get().create(level);
                if (newGhastly != null) {
                    newGhastly.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, level.random.nextFloat() * 360F, 0);
                    level.addFreshEntity(newGhastly);
                    ((ServerLevel) level).sendParticles(ParticleTypes.SOUL, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }

        if (nest.occupants.size() >= 2 && !state.getValue(com.benji.netherman.block.GhastlyNestBlock.HAS_HONEY)) {
            nest.honeyTimer++;

            if (nest.honeyTimer >= 3600) { // 3 минуты
                boolean foundRoots = false;
                for (int x = -5; x <= 5; x++) {
                    for (int y = -3; y <= 3; y++) {
                        for (int z = -5; z <= 5; z++) {
                            if (level.getBlockState(pos.offset(x, y, z)).is(Blocks.CRIMSON_ROOTS) || level.getBlockState(pos.offset(x, y, z)).is(Blocks.WARPED_ROOTS)) {
                                foundRoots = true;
                                break;
                            }
                        }
                        if (foundRoots) break;
                    }
                    if (foundRoots) break;
                }

                if (foundRoots) {
                    level.setBlock(pos, state.setValue(com.benji.netherman.block.GhastlyNestBlock.HAS_HONEY, true), 3);
                    nest.honeyTimer = 0;
                } else {
                    nest.honeyTimer = Math.max(0, nest.honeyTimer - 20);
                }
            }
        }
    }

    public boolean addGhastly(GhastlyEntity ghastly) {
        if (this.occupants.size() >= 5) return false;
        CompoundTag tag = new CompoundTag();
        ghastly.save(tag);
        this.occupants.add(new OccupantInfo(tag, 0));
        ghastly.discard();
        return true;
    }

    private void releaseGhastly(Level level, BlockPos pos, CompoundTag entityData) {
        GhastlyEntity ghastly = NetherExp.GHASTLY.get().create(level);
        if (ghastly != null) {
            ghastly.load(entityData);
            net.minecraft.core.Direction dir = this.getBlockState().getValue(com.benji.netherman.block.GhastlyNestBlock.FACING);
            BlockPos spawnPos = pos.relative(dir);
            ghastly.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
            level.addFreshEntity(ghastly);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEEHIVE_EXIT, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public void releaseAllGhastlies() {
        if (this.level == null) return;
        for (OccupantInfo info : this.occupants) {
            releaseGhastly(this.level, this.getBlockPos(), info.entityData);
        }
        this.occupants.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag listTag = new ListTag();
        for (OccupantInfo info : this.occupants) {
            CompoundTag occupantTag = new CompoundTag();
            occupantTag.put("EntityData", info.entityData);
            occupantTag.putInt("TicksInHive", info.ticksInHive);
            listTag.add(occupantTag);
        }
        tag.put("Occupants", listTag);
        tag.putInt("HoneyTimer", this.honeyTimer);
        tag.putInt("SpawnTimer", this.spawnTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.occupants.clear();
        if (tag.contains("Occupants")) {
            ListTag listTag = tag.getList("Occupants", 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag occupantTag = listTag.getCompound(i);
                this.occupants.add(new OccupantInfo(occupantTag.getCompound("EntityData"), occupantTag.getInt("TicksInHive")));
            }
        }
        this.honeyTimer = tag.getInt("HoneyTimer");
        if (tag.contains("SpawnTimer")) this.spawnTimer = tag.getInt("SpawnTimer");
    }

    private static class OccupantInfo {
        CompoundTag entityData;
        int ticksInHive;
        OccupantInfo(CompoundTag data, int ticks) {
            this.entityData = data;
            this.ticksInHive = ticks;
        }
    }
}