package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AzazelGuideBookEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> BOOK_STATE = SynchedEntityData.defineId(AzazelGuideBookEntity.class, EntityDataSerializers.INT);

    private int animationTimer = 0;

    public AzazelGuideBookEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BOOK_STATE, 0);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.tickCount == 1 && this.entityData.get(BOOK_STATE) == 0) {
                this.animationTimer = 15;

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.4, this.getZ(), 15, 0.2, 0.2, 0.2, 0.02);
                    serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.4, this.getZ(), 10, 0.1, 0.1, 0.1, 0.01);
                }
            }
        }

        Player nearestPlayer = this.level().getNearestPlayer(this, 10.0D);
        if (nearestPlayer != null) {
            double d0 = nearestPlayer.getX() - this.getX();
            double d1 = nearestPlayer.getZ() - this.getZ();
            float yaw = (float)(Mth.atan2(d1, d0) * (180.0D / Math.PI)) - 90.0F;

            this.setYRot(yaw);
            this.setYBodyRot(yaw);
            this.setYHeadRot(yaw);
            this.yRotO = yaw;

            double d2 = nearestPlayer.getEyeY() - this.getEyeY();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1);
            float pitch = (float)(-(Mth.atan2(d2, d3) * (180.0D / Math.PI)));
            this.setXRot(pitch);
            this.xRotO = pitch;
        }

        if (!this.level().isClientSide() && this.animationTimer > 0) {
            this.animationTimer--;
            if (this.animationTimer == 0) {
                int currentState = this.entityData.get(BOOK_STATE);

                if (currentState == 0) {
                    this.entityData.set(BOOK_STATE, 1);
                } else if (currentState >= 12 && currentState <= 16) {
                    this.entityData.set(BOOK_STATE, currentState - 10);
                } else if (currentState >= 21 && currentState <= 25) {
                    this.entityData.set(BOOK_STATE, currentState - 20);
                } else if (currentState == 99) {
                    this.spawnAtLocation(NetherExp.AZAZEL_GUIDE_BOOK_ITEM.get());
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 15, 0.2, 0.2, 0.2, 0.02);
                    }
                    this.discard();
                }
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            int state = this.entityData.get(BOOK_STATE);

            if (state >= 1 && state < 6 && this.animationTimer == 0) {
                int nextPage = state + 1;
                this.entityData.set(BOOK_STATE, 10 + nextPage);
                this.animationTimer = 15;
                this.level().playSound(null, this.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.NEUTRAL, 1.0F, 1.0F + (this.random.nextFloat() * 0.1F));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide() && source.getEntity() instanceof Player player) {
            int state = this.entityData.get(BOOK_STATE);

            if (this.animationTimer == 0) {
                if (player.isShiftKeyDown()) {
                    if (state >= 1 && state <= 6) {
                        this.entityData.set(BOOK_STATE, 99);
                        this.animationTimer = 10;
                        this.level().playSound(null, this.blockPosition(), SoundEvents.BOOK_PUT, SoundSource.NEUTRAL, 1.2F, 0.8F);
                    }
                } else {
                    if (state > 1 && state <= 6) {
                        int prevPage = state - 1;
                        this.entityData.set(BOOK_STATE, 20 + prevPage);
                        this.animationTimer = 15;
                        this.level().playSound(null, this.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.NEUTRAL, 1.0F, 0.85F + (this.random.nextFloat() * 0.1F));
                    }
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) { return true; }

    @Override
    public void travel(Vec3 travelVector) {
        this.setDeltaMovement(Vec3.ZERO);
        if (this.isControlledByLocalInstance()) {
            super.travel(travelVector);
        }
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "book_controller", 3, event -> {
            int state = this.entityData.get(BOOK_STATE);
            switch (state) {
                case 0: return event.setAndContinue(RawAnimation.begin().thenPlay("open"));
                case 1: return event.setAndContinue(RawAnimation.begin().thenLoop("page1_idle"));
                case 2: return event.setAndContinue(RawAnimation.begin().thenLoop("page2_idle"));
                case 3: return event.setAndContinue(RawAnimation.begin().thenLoop("page3_idle"));
                case 4: return event.setAndContinue(RawAnimation.begin().thenLoop("page4_idle"));
                case 5: return event.setAndContinue(RawAnimation.begin().thenLoop("page5_idle"));
                case 6: return event.setAndContinue(RawAnimation.begin().thenLoop("page6_idle"));

                case 12: return event.setAndContinue(RawAnimation.begin().thenPlay("move_to_page2"));
                case 13: return event.setAndContinue(RawAnimation.begin().thenPlay("move_to_page3"));
                case 14: return event.setAndContinue(RawAnimation.begin().thenPlay("move_to_page4"));
                case 15: return event.setAndContinue(RawAnimation.begin().thenPlay("move_to_page5"));
                case 16: return event.setAndContinue(RawAnimation.begin().thenPlay("move_to_page6"));

                case 21: return event.setAndContinue(RawAnimation.begin().thenPlay("back_to_page1"));
                case 22: return event.setAndContinue(RawAnimation.begin().thenPlay("back_to_page2"));
                case 23: return event.setAndContinue(RawAnimation.begin().thenPlay("back_to_page3"));
                case 24: return event.setAndContinue(RawAnimation.begin().thenPlay("back_to_page4"));
                case 25: return event.setAndContinue(RawAnimation.begin().thenPlay("back_to_page5"));

                case 99: return event.setAndContinue(RawAnimation.begin().thenPlay("close"));
                default: return event.setAndContinue(RawAnimation.begin().thenLoop("close_idle"));
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}