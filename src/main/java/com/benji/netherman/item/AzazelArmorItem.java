package com.benji.netherman.item;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.renderer.AzazelArmorRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class AzazelArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AzazelArmorItem(ArmorItem.Type type, Properties properties) {
        super(AzazelArmorMaterial.INSTANCE, type, properties.fireResistant());
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return this.type == ArmorItem.Type.CHESTPLATE;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide) {
            if ((flightTicks + 1) % 20 == 0) {
                stack.hurtAndBreak(1, entity, (e) -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
            }
        }
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 10, event -> {
            net.minecraft.world.entity.Entity entity = event.getData(DataTickets.ENTITY);

            if (entity instanceof LivingEntity wearer) {
                if (wearer.isFallFlying()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("wings_fly"));
                }
                else if (wearer.fallDistance > 0.5F && !wearer.onGround()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("fall"));
                }
                else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("wings_idle"));
                }
            }

            return PlayState.STOP;
        }));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private com.benji.netherman.client.renderer.AzazelArmorRenderer renderer;

            @Override
            public net.minecraft.client.model.HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, net.minecraft.world.entity.EquipmentSlot equipmentSlot, net.minecraft.client.model.HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new com.benji.netherman.client.renderer.AzazelArmorRenderer();
                }

                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {

        ArmorItem.Type type = this.getType();

        switch (type) {
            case HELMET -> {
                tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_helmet.line1").withStyle(ChatFormatting.GOLD));
                tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_helmet.line2").withStyle(ChatFormatting.YELLOW));
            }
            case CHESTPLATE -> {
                tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_chestplate.line1").withStyle(ChatFormatting.GOLD));
                tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_chestplate.line2").withStyle(ChatFormatting.YELLOW));
            }
            case LEGGINGS -> {
                tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_leggings.line1").withStyle(ChatFormatting.GOLD));
                tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_leggings.line2").withStyle(ChatFormatting.YELLOW));
            }
            case BOOTS -> {
                tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_boots.line1").withStyle(ChatFormatting.GOLD));
                tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_boots.line2").withStyle(ChatFormatting.YELLOW));
            }
        }

        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("tooltip.netherman.azazel_set_bonus").withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}