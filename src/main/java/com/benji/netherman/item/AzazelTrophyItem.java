package com.benji.netherman.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AzazelTrophyItem extends BlockItem {

    public AzazelTrophyItem(Block block, Properties properties) {
        super(block, properties.fireResistant().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component trophy = Component.translatable("tooltip.netherman.trophy")
                .withStyle(ChatFormatting.YELLOW);

        tooltipComponents.add(Component.translatable("tooltip.netherman.trophy.line1", trophy)
                .withStyle(ChatFormatting.GOLD));

        tooltipComponents.add(trophy);
    }

    public static float getMaskStageProperty(ItemStack stack, @Nullable net.minecraft.client.multiplayer.ClientLevel level, @Nullable net.minecraft.world.entity.LivingEntity entity, int seed) {
        return stack.hasTag() ? stack.getTag().getInt("TrophyStage") : 0.0F;
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }
}