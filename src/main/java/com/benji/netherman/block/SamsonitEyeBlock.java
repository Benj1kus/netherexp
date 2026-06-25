package com.benji.netherman.block;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.BellGuardianEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SamsonitEyeBlock extends Block {

    public SamsonitEyeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof Player) {

            level.setBlock(pos, NetherExp.SAMSONIT.get().defaultBlockState(), 3);

            level.playSound(null, pos, ModSounds.BELL_BEAST_1.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

            int radiusH = 100;
            int radiusV = 50;

            for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-radiusH, -radiusV, -radiusH), pos.offset(radiusH, radiusV, radiusH))) {
                BlockState checkState = level.getBlockState(checkPos);

                if (checkState.is(NetherExp.LABYRINTH_BELLSPAWN.get())) {

                    BellGuardianEntity guardian = NetherExp.BELL_GUARDIAN.get().create(level);
                    if (guardian != null) {
                        guardian.moveTo(checkPos.getX() + 0.5, checkPos.getY() + 1.0, checkPos.getZ() + 0.5, 0.0F, 0.0F);
                        level.addFreshEntity(guardian);

                        level.playSound(null, checkPos, ModSounds.BELL_BEAST_1.get(), SoundSource.HOSTILE, 1.0F, 0.5F);

                         level.setBlock(checkPos, NetherExp.SAMSONIT_BELL.get().defaultBlockState(), 3);
                    }
                }
            }
        }

        super.stepOn(level, pos, state, entity);
    }
}