package com.benji.netherman.item;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class AzazelTier implements Tier {
    public static final AzazelTier INSTANCE = new AzazelTier();

    @Override
    public int getUses() { return 4200; }

    @Override
    public float getSpeed() { return 9.0F; }

    @Override
    public float getAttackDamageBonus() { return 0.0F; }

    @Override
    public int getLevel() { return 4; }

    @Override
    public int getEnchantmentValue() { return 22; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.NETHERITE_INGOT);
    }
}