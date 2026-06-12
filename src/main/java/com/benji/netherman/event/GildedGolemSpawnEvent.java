package com.benji.netherman.event;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.GildedGolemEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetherExp.MODID)
public class GildedGolemSpawnEvent {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockState state = event.getPlacedBlock();
        BlockPos headPos = event.getPos();
        if (state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN)) {
            BlockPos chestPos = headPos.below();
            if (level.getBlockState(chestPos).is(Blocks.GOLD_BLOCK)) {
                BlockPos legPos = chestPos.below();
                if (level.getBlockState(legPos).is(Blocks.GOLD_BLOCK)) {
                    boolean isXAxis = level.getBlockState(chestPos.east()).is(Blocks.GOLD_BLOCK) &&
                            level.getBlockState(chestPos.west()).is(Blocks.GOLD_BLOCK);

                    boolean isZAxis = level.getBlockState(chestPos.north()).is(Blocks.GOLD_BLOCK) &&
                            level.getBlockState(chestPos.south()).is(Blocks.GOLD_BLOCK);

                    if (isXAxis || isZAxis) {
                        level.destroyBlock(headPos, false);
                        level.destroyBlock(chestPos, false);
                        level.destroyBlock(legPos, false);

                        if (isXAxis) {
                            level.destroyBlock(chestPos.east(), false);
                            level.destroyBlock(chestPos.west(), false);
                        } else {
                            level.destroyBlock(chestPos.north(), false);
                            level.destroyBlock(chestPos.south(), false);
                        }
                        GildedGolemEntity golem = NetherExp.GILDED_GOLEM.get().create(level);
                        if (golem != null) {
                            golem.moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5, 0, 0);
                            if (event.getEntity() instanceof Player player) {
                                golem.setCreatorUUID(player.getUUID());
                            }

                            level.addFreshEntity(golem);
                            if (level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, chestPos.getX() + 0.5, chestPos.getY() + 1.0, chestPos.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.1);
                            }
                        }
                    }
                }
            }
        }
    }
}