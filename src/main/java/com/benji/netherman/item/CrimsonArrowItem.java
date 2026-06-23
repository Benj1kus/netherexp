package com.benji.netherman.item;

import com.benji.netherman.entity.CrimsonArrowEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrimsonArrowItem extends ArrowItem {
    public CrimsonArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component arrowb = Component.translatable("tooltip.netherman.arrow")
                .withStyle(ChatFormatting.WHITE);
        tooltipComponents.add(arrowb);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new CrimsonArrowEntity(level, shooter);
    }
}