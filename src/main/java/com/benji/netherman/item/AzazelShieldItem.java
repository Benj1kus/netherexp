package com.benji.netherman.item;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class AzazelShieldItem extends ShieldItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AzazelShieldItem(Properties properties) {
        super(properties.defaultDurability(2000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            level.playSound(null, player.blockPosition(), ModSounds.SMOKE_BREATH.get(), SoundSource.PLAYERS, 2.0F, 1.0F);

            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "ability");

            for (int i = 0; i < 60; i++) {
                double offsetX = (level.random.nextDouble() - 0.5D) * 10.0D;
                double offsetZ = (level.random.nextDouble() - 0.5D) * 10.0D;
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        player.getX() + offsetX, player.getY() + 1.0D, player.getZ() + offsetZ,
                        2, 0.0D, 0.0D, 0.0D, 0.05D);
            }

            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D));
            for (LivingEntity target : targets) {
                if (target != player) {
                    target.addEffect(new MobEffectInstance(NetherExp.MANIPULATION_EFFECT.get(), 400, 0)); // 20 сек
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 1)); // 20 сек
                    target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 400, 0)); // 20 сек
                }
            }

            stack.hurtAndBreak(10, player, (p) -> p.broadcastBreakEvent(hand));

            player.getCooldowns().addCooldown(this, 200);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component shield = Component.translatable("tooltip.netherman.shield")
                .withStyle(ChatFormatting.GOLD);
        tooltipComponents.add(shield);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(net.minecraft.world.item.Items.NETHERITE_INGOT) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<AzazelShieldItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new com.benji.netherman.client.model.AzazelShieldModel());
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