package com.benji.netherman.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class WelcomerEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Boolean> IS_CASTING = SynchedEntityData.defineId(WelcomerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SPAWNING = SynchedEntityData.defineId(WelcomerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_WELCOMING = SynchedEntityData.defineId(WelcomerEntity.class, EntityDataSerializers.BOOLEAN);

    private int spawnTicks = 0;
    private boolean wasCasting = false;
    private int welcomeTicks = 0;

    public WelcomerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CASTING, false);
        this.entityData.define(IS_SPAWNING, false);
        this.entityData.define(IS_WELCOMING, false);
    }

    public boolean isWelcoming() { return this.entityData.get(IS_WELCOMING); }
    public void setWelcoming(boolean welcoming) { this.entityData.set(IS_WELCOMING, welcoming); }

    public boolean isSpawning() { return this.entityData.get(IS_SPAWNING); }
    public void setSpawning(boolean spawning) { this.entityData.set(IS_SPAWNING, spawning); }

    public boolean isCasting() { return this.entityData.get(IS_CASTING); }
    public void setCasting(boolean casting) { this.entityData.set(IS_CASTING, casting); }
    public void startSpawning() {
        this.setSpawning(true);
        this.spawnTicks = 20;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0F, 1.0F));
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entityIn) {}

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.spawnTicks > 0) {
                this.spawnTicks--;
                if (this.spawnTicks == 0) {
                    this.setSpawning(false);
                }
                return;
            }
            if (this.tickCount % 10 == 0) {
                AABB searchBox = this.getBoundingBox().inflate(30.0);
                List<GuardianEntity> guardians = this.level().getEntitiesOfClass(GuardianEntity.class, searchBox);

                List<GhastlyEntity> ghastlies = this.level().getEntitiesOfClass(GhastlyEntity.class, searchBox);
                boolean hasGhastly = false;
                for (GhastlyEntity g : ghastlies) {
                    if (g.isTame()) {
                        hasGhastly = true;
                        break;
                    }
                }

                boolean foundAggressive = false;
                for (GuardianEntity guardian : guardians) {
                    if (guardian.getTarget() != null) {
                        foundAggressive = true;
                        break;
                    }
                }
                if (foundAggressive) {
                    this.setCasting(true);
                    this.setWelcoming(false);
                } else if (hasGhastly) {
                    this.setCasting(false);
                    if (!this.isWelcoming()) {
                        this.welcomeTicks = 20;
                    }
                    this.setWelcoming(true);
                } else {
                    this.setCasting(false);
                    this.setWelcoming(false);
                }
            }
            boolean currentCasting = this.isCasting();
            if (currentCasting && !this.wasCasting) {
                this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 1.0F);
            }
            this.wasCasting = currentCasting;

        } else {
            if (this.isCasting()) {
                for (int i = 0; i < 2; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
                    double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;
                    this.level().addParticle(ParticleTypes.ENTITY_EFFECT,
                            this.getX() + offsetX, this.getY() + 2.2, this.getZ() + offsetZ,
                            1.0D, 0.0D, 0.0D); 
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<WelcomerEntity> controller = new AnimationController<>(this, "controller", 5, event -> {
            if (this.isSpawning()) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("spawn"));
            }
            if (this.isCasting()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("magic_loop"));
            }
            if (this.isWelcoming()) {
                if (this.welcomeTicks > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("welcome"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("welcome_loop"));
                }
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        });

        if (this.isSpawning()) {
            controller.setAnimation(RawAnimation.begin().thenPlay("spawn"));
        } else if (this.isWelcoming() && this.welcomeTicks > 0) {
            controller.setAnimation(RawAnimation.begin().thenPlay("welcome"));
        }

        controllers.add(controller);
    }


    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.EVOKER_HURT;
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}