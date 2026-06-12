package com.benji.netherman.event;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetherExp.MODID)
public class AzazelAltarEvent {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockPos placedPos = event.getPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos centerPos = placedPos.offset(dx, 0, dz);
                if (level.getBlockState(centerPos).is(NetherExp.NETHER_SPAWNER.get())) {
                    checkAndSpawnAzazel((ServerLevel) level, centerPos);
                }
            }
        }
    }

    private static void checkAndSpawnAzazel(ServerLevel level, BlockPos center) {
        if (!level.getBlockState(center.north()).is(Blocks.GOLD_BLOCK)) return;
        if (!level.getBlockState(center.south()).is(Blocks.GOLD_BLOCK)) return;
        if (!level.getBlockState(center.east()).is(Blocks.GOLD_BLOCK)) return;
        if (!level.getBlockState(center.west()).is(Blocks.GOLD_BLOCK)) return;
        if (!level.getBlockState(center.north().east()).is(Blocks.ANCIENT_DEBRIS)) return;
        if (!level.getBlockState(center.north().west()).is(Blocks.ANCIENT_DEBRIS)) return;
        if (!level.getBlockState(center.south().east()).is(Blocks.ANCIENT_DEBRIS)) return;
        if (!level.getBlockState(center.south().west()).is(Blocks.ANCIENT_DEBRIS)) return;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                level.destroyBlock(pos, false);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05);
                level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.05);
            }
        }
        AzazelEntity azazel = NetherExp.AZAZEL.get().create(level);
        if (azazel != null) {
            azazel.moveTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5, 0, 0);
            azazel.getEntityData().set(AzazelEntity.ATTACK_STATE, 10);
            azazel.getEntityData().set(AzazelEntity.MERCY_TICK, 0);

            level.addFreshEntity(azazel);
        }
    }
}