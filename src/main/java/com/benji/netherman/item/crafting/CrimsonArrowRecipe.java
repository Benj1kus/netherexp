package com.benji.netherman.item.crafting;

import com.benji.netherman.NetherExp;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class CrimsonArrowRecipe extends CustomRecipe {
    public CrimsonArrowRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int bottles = 0;
        int arrows = 0;

        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(NetherExp.CRIMSON_HONEY_BOTTLE.get())) {
                    bottles++;
                } else if (stack.is(Items.ARROW)) {
                    arrows += stack.getCount();
                } else {
                    return false;
                }
            }
        }
        return bottles == 1 && arrows >= 1 && arrows <= 5;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        int arrows = 0;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (stack.is(Items.ARROW)) {
                arrows += stack.getCount();
            }
        }
        return new ItemStack(NetherExp.CRIMSON_ARROW_ITEM.get(), arrows);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(net.minecraft.world.inventory.CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(NetherExp.CRIMSON_HONEY_BOTTLE.get())) {
                    remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
                }
                else if (stack.is(Items.ARROW)) {
                    stack.setCount(1);
                }
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NetherExp.CRIMSON_ARROW_CRAFTING.get();
    }
}