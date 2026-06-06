package com.benji.netherman.item;

import com.benji.netherman.entity.CrimsonArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrimsonArrowItem extends ArrowItem {
    public CrimsonArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new CrimsonArrowEntity(level, shooter);
    }
}