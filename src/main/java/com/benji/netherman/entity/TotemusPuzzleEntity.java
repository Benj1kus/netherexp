package com.benji.netherman.entity;

import com.benji.netherman.block.entity.TotemusHoleBlockEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TotemusPuzzleEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(TotemusPuzzleEntity.class, EntityDataSerializers.INT);

    public TotemusPuzzleEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(COLOR, 0);
    }

    public int getColor() { return this.entityData.get(COLOR); }
    public void setColor(int color) { this.entityData.set(COLOR, color); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public void tick() {
        super.tick();
        this.yBodyRot = this.yHeadRot;
        if (this.level().isClientSide() && this.tickCount < 20) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D),
                        0.0D, 0.05D, 0.0D);
            }
        }
    }


    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide()) {
            BlockEntity be = this.level().getBlockEntity(this.blockPosition().below());
            if (be instanceof TotemusHoleBlockEntity hole) {
                Player player = cause.getEntity() instanceof Player p ? p : null;
                hole.reportDeath(this.getColor(), player);
            }
            this.playSound(SoundEvents.WITHER_SKELETON_DEATH, 1.0F, 1.0F);
        }
        super.die(cause);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlay("spawn").thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("PuzzleColor", this.getColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setColor(compound.getInt("PuzzleColor"));
    }
}