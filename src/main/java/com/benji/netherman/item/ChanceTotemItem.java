package com.benji.netherman.item;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.GuardianEntity;
import com.benji.netherman.network.ModMessages;
import com.benji.netherman.network.TotemAnimationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ChanceTotemItem extends Item {

    public ChanceTotemItem(Properties properties) {
        super(properties.defaultDurability(2));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component spawnpoint = Component.translatable("tooltip.netherman.chance_totem.spawnpoint")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);

        tooltipComponents.add(Component.translatable("tooltip.netherman.chance_totem.line1", spawnpoint)
                .withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(Component.translatable("tooltip.netherman.chance_totem.line2")
                .withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                GuardianEntity guardian = NetherExp.GUARDIAN.get().create(level);
                if (guardian != null) {
                    guardian.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

                    guardian.setOwnerUUID(player.getUUID());

                    guardian.startSpawning();
                    level.addFreshEntity(guardian);
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    ModMessages.sendToPlayer(new TotemAnimationPacket(new ItemStack(this)), serverPlayer);
                }

                stack.hurtAndBreak(1, player, (p) -> {
                    p.broadcastBreakEvent(context.getHand());
                    level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                });
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }
}