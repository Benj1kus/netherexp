package com.benji.netherman.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class AzazelTrophyItem extends BlockItem {

    public AzazelTrophyItem(Block block, Properties properties) {
        super(block, properties.fireResistant().stacksTo(1));
    }

    public static float getMaskStageProperty(ItemStack stack, @Nullable net.minecraft.client.multiplayer.ClientLevel level, @Nullable net.minecraft.world.entity.LivingEntity entity, int seed) {
        return stack.hasTag() ? stack.getTag().getInt("TrophyStage") : 0.0F;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }
}