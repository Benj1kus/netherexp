package com.benji.netherman.item;

import com.benji.netherman.client.gui.QuotaScreen;
import com.benji.netherman.QuotaManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class QuotaItem extends Item {

    public QuotaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            CompoundTag data = player.getPersistentData();

            for (int i = 1; i <= 3; i++) {
                if (data.contains("QuotaTask" + i) && data.getInt("QuotaTask" + i) == 3) {
                    int prog = data.getInt("QuotaProg" + i);
                    int max = data.getInt("QuotaMax" + i);
                    int needed = max - prog;

                    if (needed > 0) {
                        int taken = 0;
                        for (int j = 0; j < player.getInventory().getContainerSize(); j++) {
                            ItemStack invStack = player.getInventory().getItem(j);
                            if (invStack.is(Items.GOLD_INGOT)) {
                                int toTake = Math.min(invStack.getCount(), needed - taken);
                                invStack.shrink(toTake);
                                taken += toTake;
                                if (taken >= needed) break;
                            }
                        }
                        if (taken > 0) {
                            QuotaManager.addProgress(player, 3, taken);
                            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
                        }
                    }
                }
            }

            stack.getOrCreateTag().put("QuotaData", data);

            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            com.benji.netherman.client.ClientActionDelegate.openQuotaScreen(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}