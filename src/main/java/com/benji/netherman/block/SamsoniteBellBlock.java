package com.benji.netherman.block;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.SamsoniteBellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// ДЛЯ 1.21.1: Не забудь добавить CODEC, как мы делали в других блоках!
public class SamsoniteBellBlock extends BellBlock {

    public SamsoniteBellBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SamsoniteBellBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, NetherExp.SAMSONITE_BELL_BE.get(), SamsoniteBellBlockEntity::tick);
    }
}