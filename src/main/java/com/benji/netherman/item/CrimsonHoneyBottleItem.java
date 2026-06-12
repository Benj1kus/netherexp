package com.benji.netherman.item;

import com.benji.netherman.NetherExp;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CrimsonHoneyBottleItem extends Item {
    public CrimsonHoneyBottleItem(Properties properties) {
        super(properties);
    }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        super.finishUsingItem(stack, level, entityLiving);

        if (entityLiving instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }
        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (entityLiving instanceof Player player && !player.getAbilities().instabuild) {
                ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(emptyBottle)) {
                    player.drop(emptyBottle, false);
                }
            }
            return stack;
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public net.minecraft.sounds.SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public net.minecraft.sounds.SoundEvent getEatingSound() {
        return SoundEvents.HONEY_DRINK;
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (level.getBlockState(pos).is(Blocks.HONEY_BLOCK)) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, NetherExp.CRIMSON_HONEY_BLOCK.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 0.5F);
                ((ServerLevel) level).sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, NetherExp.CRIMSON_HONEY_BLOCK.get().defaultBlockState()),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        30, 0.3, 0.3, 0.3, 0.05
                );
                if (player != null && !player.getAbilities().instabuild) {
                    stack.shrink(1);
                    ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().add(emptyBottle)) {
                        player.drop(emptyBottle, false);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }
}