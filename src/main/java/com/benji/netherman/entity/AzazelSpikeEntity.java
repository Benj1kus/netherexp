package com.benji.netherman.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AzazelSpikeEntity extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTicks = 0;

    
    public int animState = 0;

    public AzazelSpikeEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        
        if (lifeTicks == 20) {
            animState = 1;
        }

        
        if (lifeTicks >= 40) {
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY(), this.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.0D);
            }
            this.discard();
            return;
        }


        if (lifeTicks < 25 && !this.level().isClientSide() && this.tickCount % 5 == 0) {
            AABB hitbox = this.getBoundingBox().inflate(0.2D);
            float spikeDamage = com.benji.netherman.config.AzazelConfig.HUMAN_SPIKE_DAMAGE.get().floatValue();
            int witherDuration = com.benji.netherman.config.AzazelConfig.HUMAN_SPIKE_WITHER_DURATION.get();

            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, hitbox)) {
                if (target == this.getOwner() || target instanceof AzazelHumanEntity) continue;

                target.hurt(this.damageSources().magic(), spikeDamage);

                if (witherDuration > 0) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, witherDuration, 1));
                }
            }
        }

        
        if (lifeTicks == 1 && this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY(), this.getZ(), 15, 0.5D, 0.0D, 0.5D, 0.0D);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            if (animState == 0) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("spawn"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlay("despawn"));
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}