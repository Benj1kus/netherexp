package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.ModSounds;
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
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class TotemusBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = CAVE (Fear), 1 = CITY (Excitement), 2 = CHURCH (Faith), 3 = BOSS ARENA (Clear)
    private int totemType = 0;
    private int scanTimer = 0;

    public TotemusBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.TOTEMUS_BE.get(), pos, state);
    }

    public int getTotemType() { return this.totemType; }

    public static void tick(Level level, BlockPos pos, BlockState state, TotemusBlockEntity entity) {
        if (level.isClientSide()) return;

        entity.scanTimer--;
        if (entity.scanTimer <= 0) {
            entity.scanTimer = 20; // Проверяем всё раз в секунду

            // 1. ПРОВЕРКА БЛОКОВ ВОКРУГ (3x3x3)
            int newType = 0;
            for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockState neighbor = level.getBlockState(checkPos);

                // Расставляем приоритеты: Blackstone Column (3) > Netherite (2) > Gold (1)
                if (neighbor.is(NetherExp.BLACKSTONE_COLUMN.get())) {
                    newType = 3;
                    break; // Высший приоритет, сразу прерываем поиск
                } else if (neighbor.is(Blocks.NETHERITE_BLOCK) && newType < 2) {
                    newType = 2;
                } else if (neighbor.is(Blocks.GOLD_BLOCK) && newType < 1) {
                    newType = 1;
                }
            }

            // Обновляем состояние для текстуры
            if (entity.totemType != newType) {
                entity.totemType = newType;
                entity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }

            // 2. ВЫДАЧА ИЛИ СНЯТИЕ ЭФФЕКТОВ И ТИТРОВ В РАДИУСЕ 20 БЛОКОВ
            AABB box = new AABB(pos).inflate(20.0);
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box);

            for (ServerPlayer player : players) {

                if (entity.totemType == 3) {
                    // --- ЛОГИКА 3 ТИПА: ОЧИЩЕНИЕ ПЕРЕД БОССОМ ---

                    // Сработает только один раз (когда у игрока еще висит какой-то эффект зоны)
                    if (player.hasEffect(NetherExp.FEAR_EFFECT.get()) ||
                            player.hasEffect(NetherExp.EXCITEMENT_EFFECT.get()) ||
                            player.hasEffect(NetherExp.FAITH_EFFECT.get())) {

                        // Снимаем эффекты (эмбиент мгновенно затухнет благодаря нашему умному звуку!)
                        player.removeEffect(NetherExp.FEAR_EFFECT.get());
                        player.removeEffect(NetherExp.EXCITEMENT_EFFECT.get());
                        player.removeEffect(NetherExp.FAITH_EFFECT.get());

                        // Проигрываем ударный звук
                        level.playSound(null, player.blockPosition(), ModSounds.BIG_TEXT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                        // Чтобы убрать "ZONE", мы помещаем "YOU ARRIVED" в Title (главный большой текст), а Subtitle оставляем пустым
                        Component title = Component.literal("YOU ARRIVED").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD);
                        Component subtitle = Component.empty();

                        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
                        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                        player.connection.send(new ClientboundSetTitleTextPacket(title));
                    }
                } else {
                    // --- ЛОГИКА 0, 1, 2 ТИПОВ: НАЛОЖЕНИЕ ЭФФЕКТОВ ---
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

                        Component title = Component.literal("ZONE").withStyle(ChatFormatting.YELLOW);
                        Component subtitle = switch (entity.totemType) {
                            case 2 -> Component.literal("AZAZELS LAIR").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
                            case 1 -> Component.literal("THE SACRED CITY").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
                            default -> Component.literal("THE CURSED QUARRIES").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

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