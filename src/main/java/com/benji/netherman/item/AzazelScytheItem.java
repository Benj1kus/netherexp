package com.benji.netherman.item;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelSplashEntity;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class AzazelScytheItem extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AzazelScytheItem(Properties properties) {
        super(AzazelTier.INSTANCE, 19, -3.0F, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            if (player.isShiftKeyDown()) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "spear_mode");

                level.playSound(null, player.blockPosition(), ModSounds.HIRRING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                stack.getOrCreateTag().putInt("TransformTimer", 15);
                return InteractionResultHolder.success(stack);
            }
            else {
                AzazelSplashEntity splash = NetherExp.SPLASH_ENTITY.get().create(level);
                if (splash != null) {
                    Vec3 look = player.getLookAngle();
                    splash.setPos(player.getX() + look.x * 1.5, player.getEyeY() - 0.2, player.getZ() + look.z * 1.5);
                    splash.setOwner(player);
                    splash.shoot(look.x, look.y, look.z, 1.5F, 0.0F);
                    level.addFreshEntity(splash);
                }

                level.playSound(null, player.blockPosition(), ModSounds.SWING_2.get(), SoundSource.PLAYERS, 2.0F, 0.8F);

                stack.hurtAndBreak(5, player, (p) -> p.broadcastBreakEvent(hand));
                player.getCooldowns().addCooldown(this, 100);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide() && attacker instanceof Player player) {
            SoundEvent swingSound = player.getRandom().nextBoolean() ? ModSounds.SWING_1.get() : ModSounds.SWING_2.get();
            player.level().playSound(null, player.blockPosition(), swingSound, SoundSource.PLAYERS, 1.5F, 1.0F);
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
                        ItemStack spearStack = new ItemStack(NetherExp.AZAZEL_SPEAR.get());
                        spearStack.setTag(stack.getTag().copy());
                        spearStack.getTag().remove("TransformTimer");
                        player.getInventory().setItem(slotId, spearStack);
                    }
                }
            }
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<AzazelScytheItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new com.benji.netherman.client.model.AzazelScytheModel());
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