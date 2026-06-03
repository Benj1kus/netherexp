package com.benji.netherman.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class NoteItem extends Item {
    public NoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            // Звук перелистывания бумаги
            level.playSound(player, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Открываем GUI строго на клиенте (используем DistExecutor, чтобы не крашнуть сервер)
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.benji.netherman.client.gui.NoteScreen.openScreen());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}