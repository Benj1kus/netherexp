package com.benji.netherman.item;

import com.benji.netherman.NetherExp;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AltarCompassKeyItem extends Item {

    public AltarCompassKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("HeadAltarMode") || super.isFoil(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (level.getBlockState(pos).is(NetherExp.SAMSONIT_KEY.get())) {
            if (!level.isClientSide()) {
                CompoundTag tag = stack.getOrCreateTag();

                if (!tag.getBoolean("HeadAltarMode")) {
                    tag.putBoolean("HeadAltarMode", true);

                    tag.remove("TargetX");
                    tag.remove("TargetZ");

                    level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0F, 0.7F);

                    
                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                    if (lightning != null) {
                        lightning.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                        lightning.setVisualOnly(true); 
                        level.addFreshEntity(lightning);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component compass = Component.translatable("tooltip.netherman.compass")
                .withStyle(ChatFormatting.GOLD);
        tooltipComponents.add(compass);
}

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            CompoundTag tag = stack.getOrCreateTag();

            boolean isHeadAltarMode = tag.getBoolean("HeadAltarMode");
            String targetStructureName = isHeadAltarMode ? "head_altar" : "monument";

            ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(NetherExp.MODID, targetStructureName));
            var registry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
            var holder = registry.getHolder(structureKey).orElse(null);

            if (holder != null) {
                Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource().getGenerator()
                        .findNearestMapStructure(serverLevel, HolderSet.direct(holder), player.blockPosition(), 100, false);

                if (pair != null) {
                    BlockPos structureOrigin = pair.getFirst();
                    BlockPos preciseTarget = structureOrigin;

                    if (!isHeadAltarMode) {
                        int scanRadius = 48;
                        boolean foundKey = false;
                        for (int x = -scanRadius; x <= scanRadius; x++) {
                            for (int y = -40; y <= 40; y++) {
                                for (int z = -scanRadius; z <= scanRadius; z++) {
                                    BlockPos checkPos = structureOrigin.offset(x, y, z);
                                    if (serverLevel.getBlockState(checkPos).is(NetherExp.SAMSONIT_KEY.get())) {
                                        preciseTarget = checkPos;
                                        foundKey = true;
                                        break;
                                    }
                                }
                                if (foundKey) break;
                            }
                            if (foundKey) break;
                        }
                    }

                    tag.putInt("TargetX", preciseTarget.getX());
                    tag.putInt("TargetZ", preciseTarget.getZ());

                    level.playSound(null, player.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    player.sendSystemMessage(Component.literal("§4§kYOU ARE ALONE"));
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}