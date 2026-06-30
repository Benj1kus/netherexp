package com.benji.netherman.item;

import com.benji.netherman.NetherExp;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class AzazelArmorMaterial implements ArmorMaterial {
    public static final AzazelArmorMaterial INSTANCE = new AzazelArmorMaterial();

    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return HEALTH_PER_SLOT[type.ordinal()] * 45;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 3;
            case CHESTPLATE -> 8;
            case LEGGINGS -> 6;
            case BOOTS -> 3;
        };
    }

    @Override
    public int getEnchantmentValue() { return 15; }

    @Override
    public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }

    @Override
    public Ingredient getRepairIngredient() { return Ingredient.of(Items.NETHERITE_INGOT); }

    @Override
    public String getName() {
        return NetherExp.MODID + ":azazel_armor";
    }

    @Override
    public float getToughness() {
        return 5.0F;
    }

    @Override
    public float getKnockbackResistance() { return 0.1F; }
}