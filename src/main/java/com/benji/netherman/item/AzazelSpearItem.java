package com.benji.netherman.item;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelHumanEntity;
import com.benji.netherman.entity.AzazelSpikeEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class AzazelSpearItem extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AzazelSpearItem(Properties properties) {
        super(AzazelTier.INSTANCE, 11, -2.4F, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            if (player.isShiftKeyDown()) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "scyth_mode");

                level.playSound(null, player.blockPosition(), ModSounds.HIRRING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                stack.getOrCreateTag().putInt("TransformTimer", 10);
                return InteractionResultHolder.success(stack);
            }
            else {
                AABB box = player.getBoundingBox().inflate(8.0D);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != player && !(e instanceof AzazelHumanEntity));

                if (!targets.isEmpty()) {
                    for (LivingEntity target : targets) {
                        AzazelSpikeEntity spike = NetherExp.SPIKE_ENTITY.get().create(level);
                        if (spike != null) {
                            spike.setPos(target.getX(), target.getY(), target.getZ());
                            spike.setOwner(player);
                            level.addFreshEntity(spike);
                        }
                    }

                    level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.5F, 0.8F);

                    stack.hurtAndBreak(5, player, (p) -> p.broadcastBreakEvent(hand));
                    player.getCooldowns().addCooldown(this, 100);
                    triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "spear_attack");
                    return InteractionResultHolder.success(stack);
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component spear = Component.translatable("tooltip.netherman.spear")
                .withStyle(ChatFormatting.DARK_RED);

        tooltipComponents.add(Component.translatable("tooltip.netherman.spear.line1", spear)
                .withStyle(ChatFormatting.GOLD));

        tooltipComponents.add(spear);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide() && attacker instanceof Player player) {
            Vec3 look = player.getLookAngle();
            target.setDeltaMovement(target.getDeltaMovement().add(look.x * 2.0, 0.4, look.z * 2.0));
            triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) target.level()), "controller", "spear_attack");
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof Player player) {
            if (stack.hasTag() && stack.getTag().contains("TransformTimer")) {
                int timer = stack.getTag().getInt("TransformTimer");
                if (timer > 0) {
                    timer--;
                    stack.getTag().putInt("TransformTimer", timer);

                    if (timer <= 0) {
                        ItemStack scytheStack = new ItemStack(NetherExp.AZAZEL_SCYTHE.get());
                        scytheStack.setTag(stack.getTag().copy());
                        scytheStack.getTag().remove("TransformTimer");
                        player.getInventory().setItem(slotId, scytheStack);
                    }
                }
            }
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<AzazelSpearItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new com.benji.netherman.client.model.AzazelSpearModel());
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}