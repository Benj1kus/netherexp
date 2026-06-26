package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.BelieverVillagerEntity;
import com.benji.netherman.entity.BellGuardianEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SamsoniteBellBlockEntity extends BellBlockEntity {
    private int cooldown = 0;

    public SamsoniteBellBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return NetherExp.SAMSONITE_BELL_BE.get();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SamsoniteBellBlockEntity entity) {
        if (level.isClientSide()) {
            BellBlockEntity.clientTick(level, pos, state, entity);
        } else {
            BellBlockEntity.serverTick(level, pos, state, entity);

            if (entity.cooldown > 0) {
                entity.cooldown--;
                return;
            }

            if (level.getGameTime() % 20 == 0) {

                AABB playerBox = new AABB(pos).inflate(10.0D);
                boolean hasPlayer = !level.getEntitiesOfClass(Player.class, playerBox).isEmpty();

                if (hasPlayer) {
                    if (level.random.nextInt(20) == 0) {

                        if (state.getBlock() instanceof BellBlock bellBlock) {
                            bellBlock.attemptToRing(null, level, pos, null);

                            AABB mobBox = new AABB(pos).inflate(50.0D);

                            List<Player> players = level.getEntitiesOfClass(Player.class, playerBox);
                            for (Player player : players) {
                                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 2));
                            }

                            List<BellGuardianEntity> mobs = level.getEntitiesOfClass(BellGuardianEntity.class, mobBox);

                            for (BellGuardianEntity mob : mobs) {
                                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
                            }

                            entity.cooldown = 200;
                        }
                    }
                }
            }
        }
    }
}