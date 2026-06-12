package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.ModSounds;
import com.benji.netherman.block.TotemusBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
public class TotemusBlockEntity extends BlockEntity {

    private int totemType = 0;
    private int scanTimer = 0;

    public TotemusBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.TOTEMUS_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TotemusBlockEntity entity) {
        if (level.isClientSide()) return;

        entity.scanTimer--;
        if (entity.scanTimer <= 0) {
            entity.scanTimer = 20;

            int newType = 0;
            for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockState neighbor = level.getBlockState(checkPos);
                if (neighbor.is(NetherExp.BLACKSTONE_COLUMN.get())) {
                    newType = 3;
                    break;
                } else if (neighbor.is(Blocks.ANCIENT_DEBRIS) && newType < 2) {
                    newType = 2;
                } else if (neighbor.is(Blocks.GOLD_BLOCK) && newType < 1) {
                    newType = 1;
                }
            }
            if (entity.totemType != newType) {
                entity.totemType = newType;
                entity.setChanged();
            }
            BlockState currentState = level.getBlockState(pos);
            if (currentState.hasProperty(TotemusBlock.TYPE) && currentState.getValue(TotemusBlock.TYPE) != entity.totemType) {
                level.setBlock(pos, currentState.setValue(TotemusBlock.TYPE, entity.totemType), 3);
            }

            AABB box = new AABB(pos).inflate(20.0);
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box);

            for (ServerPlayer player : players) {
                if (entity.totemType == 3) {
                    if (player.hasEffect(NetherExp.FEAR_EFFECT.get()) ||
                            player.hasEffect(NetherExp.EXCITEMENT_EFFECT.get()) ||
                            player.hasEffect(NetherExp.FAITH_EFFECT.get())) {

                        player.removeEffect(NetherExp.FEAR_EFFECT.get());
                        player.removeEffect(NetherExp.EXCITEMENT_EFFECT.get());
                        player.removeEffect(NetherExp.FAITH_EFFECT.get());

                        level.playSound(null, player.blockPosition(), ModSounds.BIG_TEXT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                        Component title = Component.translatable("block.netherman.totemus.arrived").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
                        Component subtitle = Component.empty();

                        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
                        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                        player.connection.send(new ClientboundSetTitleTextPacket(title));
                    }
                } else {
                    MobEffect targetEffect = switch (entity.totemType) {
                        case 2 -> NetherExp.FAITH_EFFECT.get();
                        case 1 -> NetherExp.EXCITEMENT_EFFECT.get();
                        default -> NetherExp.FEAR_EFFECT.get();
                    };

                    if (!player.hasEffect(targetEffect)) {
                        player.removeEffect(NetherExp.FEAR_EFFECT.get());
                        player.removeEffect(NetherExp.EXCITEMENT_EFFECT.get());
                        player.removeEffect(NetherExp.FAITH_EFFECT.get());

                        player.addEffect(new MobEffectInstance(targetEffect, Integer.MAX_VALUE, 0, false, false, true));

                        level.playSound(null, player.blockPosition(), ModSounds.BIG_TEXT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                        Component title = Component.translatable("block.netherman.totemus.zone").withStyle(ChatFormatting.YELLOW);
                        Component subtitle = switch (entity.totemType) {
                            case 2 -> Component.translatable("block.netherman.totemus.zone.azazel").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
                            case 1 -> Component.translatable("block.netherman.totemus.zone.city").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
                            default -> Component.translatable("block.netherman.totemus.zone.quarries").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
                        };

                        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
                        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                        player.connection.send(new ClientboundSetTitleTextPacket(title));
                    }
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TotemType", this.totemType);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.totemType = tag.getInt("TotemType");
    }

    @Override
    public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) this.load(pkt.getTag());
    }
}