package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class StatueBossunitEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int attackTimer = 300;
    private final double ATTACK_RADIUS = 20.0D;
    private static final MobEffect[] DEBUFFS = {
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.POISON,
            MobEffects.WEAKNESS,
            MobEffects.WITHER,
            MobEffects.DARKNESS
    };


    public StatueBossunitEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, (float) ATTACK_RADIUS));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override
    public void knockback(double strength, double x, double z) {}
    @Override
    public void push(double x, double y, double z) {}
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.attackTimer--;

            if (this.attackTimer <= 0) {
                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(ATTACK_RADIUS));

                if (!nearbyPlayers.isEmpty()) {
                    this.playSound(ModSounds.SPINNING_WHEEL.get(), 1.0F, this.getVoicePitch());
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                                this.getX(), this.getY() + 1.5D, this.getZ(),
                                40, 1.0D, 1.0D, 1.0D, 0.1D);
                    }
                    for (Player player : nearbyPlayers) {
                        MobEffect randomEffect = DEBUFFS[this.random.nextInt(DEBUFFS.length)];
                        int randomAmplifier = this.random.nextInt(3);
                        player.addEffect(new MobEffectInstance(randomEffect, 400, randomAmplifier));
                    }

                    this.attackTimer = 200;
                } else {
                    this.attackTimer = 20;
                }
            }
        }
    }
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.UNIT_IDLE.get();
    }

    @Override
    public float getVoicePitch() {
        return 0.8F + this.random.nextFloat() * 0.4F;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AttackTimer", this.attackTimer);
    }
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        int rand = this.random.nextInt(3);
        return rand == 0 ? ModSounds.STATUE_HURT_1.get() : (rand == 1 ? ModSounds.STATUE_HURT_2.get() : ModSounds.STATUE_HURT_3.get());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.attackTimer = tag.getInt("AttackTimer");
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}