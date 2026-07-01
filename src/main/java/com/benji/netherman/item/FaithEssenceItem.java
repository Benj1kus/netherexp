package com.benji.netherman.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.benji.netherman.NetherExp;

import java.util.List;

public class FaithEssenceItem extends Item {

    public FaithEssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component essence = Component.translatable("tooltip.netherman.essence")
                .withStyle(ChatFormatting.GOLD);

        tooltipComponents.add(Component.translatable("tooltip.netherman.essence.line1", essence)
                .withStyle(ChatFormatting.AQUA));

        tooltipComponents.add(Component.translatable("tooltip.netherman.essence.line2", essence)
                .withStyle(ChatFormatting.BLUE));

        tooltipComponents.add(essence);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (player.getPersistentData().contains("AzazelPenaltyTime")) {
                com.benji.netherman.QuotaManager.restoreHealth(player);
                player.getPersistentData().remove("AzazelPenaltyTime");
                if (!player.isCreative()) player.getItemInHand(hand).shrink(1);
                return InteractionResultHolder.consume(player.getItemInHand(hand));
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        boolean transformed = false;
        Entity newEntity = null;

        if (target instanceof Pillager) {
            newEntity = NetherExp.BELIEVER.get().create(level);
            transformed = true;
        }
        else if (target instanceof Villager) {
            if (level.random.nextFloat() < 0.10F) { // 10% шанс
                newEntity = NetherExp.WELCOMER.get().create(level);
            } else {
                newEntity = NetherExp.BELIEVER_VILLAGER.get().create(level);
            }
            transformed = true;
        }
        else if (target.getMaxHealth() < 100.0F && !(target instanceof Player)) {
            newEntity = NetherExp.STATUE.get().create(level);
            transformed = true;
        }

        if (transformed && newEntity != null) {
            newEntity.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());

            if (target.hasCustomName()) {
                newEntity.setCustomName(target.getCustomName());
            }

            level.addFreshEntity(newEntity);

            if (level instanceof ServerLevel serverLevel) {
                DustParticleOptions redMagic = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
                serverLevel.sendParticles(redMagic, target.getX(), target.getY() + 1.0D, target.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);

                serverLevel.playSound(null, target.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.2F, 0.8F);
                serverLevel.playSound(null, target.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 0.8F, 1.2F);
            }

            target.discard();

            if (target instanceof Pillager) {
                com.benji.netherman.QuotaManager.addProgress(player, 4, 1);
            } else if (target instanceof Villager) {
                com.benji.netherman.QuotaManager.addProgress(player, 5, 1);
            }

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}