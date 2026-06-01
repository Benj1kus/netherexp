package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class BelieverEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_SICK = SynchedEntityData.defineId(BelieverEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_PRAYING = SynchedEntityData.defineId(BelieverEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_PROTECTED = SynchedEntityData.defineId(BelieverEntity.class, EntityDataSerializers.BOOLEAN);

    public int protectedTimer = 0;
    private AzazelEntity currentAzazel = null;

    public BelieverEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_SICK, false);
        this.entityData.define(IS_PRAYING, false);
        this.entityData.define(IS_PROTECTED, false);
    }

    public boolean isSick() { return this.entityData.get(IS_SICK); }
    public void setSick(boolean sick) { this.entityData.set(IS_SICK, sick); }

    // Вызывается боссом Азазелем
    public void setProtected(int ticks) {
        this.protectedTimer = ticks;
        this.entityData.set(IS_PROTECTED, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    // --- НЕУЯЗВИМОСТЬ ПРИ ЗАЩИТЕ ---
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.entityData.get(IS_PROTECTED)) {
            return false; // Полностью блокируем урон
        }
        return super.hurt(source, amount);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.GUNPOWDER) && !this.isSick()) {
            if (!player.isCreative()) stack.shrink(1);
            this.setSick(true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (stack.is(Items.GOLDEN_APPLE) && this.isSick()) {
            if (!player.isCreative()) stack.shrink(1);
            this.setSick(false);
            this.playSound(SoundEvents.PILLAGER_CELEBRATE, 1.0F, 1.0F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.0, this.getZ(), 15, 0.3, 0.3, 0.3, 0.0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    // --- ЛОГИКА ОБЕЗДВИЖИВАНИЯ ---
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isSick() || this.entityData.get(IS_PRAYING)) {
            this.getNavigation().stop();
            super.travel(Vec3.ZERO);
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            // 1. Таймер защиты
            if (this.protectedTimer > 0) {
                this.protectedTimer--;
                if (this.protectedTimer <= 0) {
                    this.entityData.set(IS_PROTECTED, false);
                }
            }

            // 2. Чихание
            if (this.isSick() && this.random.nextInt(80) == 0) {
                this.playSound(com.benji.netherman.ModSounds.SNEEZE.get(), 1.0F, this.getVoicePitch());
            }

            // 3. Поиск Азазеля (каждую секунду)
            if (this.tickCount % 20 == 0) {
                List<AzazelEntity> azazels = this.level().getEntitiesOfClass(AzazelEntity.class, this.getBoundingBox().inflate(30.0D));
                if (!azazels.isEmpty()) {
                    this.currentAzazel = azazels.get(0);
                    this.entityData.set(IS_PRAYING, true);
                } else {
                    this.currentAzazel = null;
                    this.entityData.set(IS_PRAYING, false);
                }
            }

            // 4. Лечение и Партиклы-нити
            if (this.currentAzazel != null && this.currentAzazel.isAlive()) {
                this.getLookControl().setLookAt(this.currentAzazel);

                // Лечит босса на 5 ХП каждую секунду
                if (this.tickCount % 20 == 0) {
                    this.currentAzazel.heal(5.0F);
                }

                // Линия редстоуна к боссу (каждые 5 тиков)
                if (this.tickCount % 5 == 0 && this.level() instanceof ServerLevel serverLevel) {
                    Vec3 start = this.position().add(0, 1.0, 0); // От груди Believer
                    Vec3 end = this.currentAzazel.position().add(0, 2.0, 0); // К груди Босса
                    double distance = start.distanceTo(end);
                    Vec3 dir = end.subtract(start).normalize();

                    // Рисуем линию
                    for (double d = 0; d < distance; d += 0.5) {
                        Vec3 pos = start.add(dir.scale(d));
                        serverLevel.sendParticles(DustParticleOptions.REDSTONE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                    }
                }
            } else {
                this.entityData.set(IS_PRAYING, false);
            }
        }
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            if (cause.getEntity() instanceof Player killer) {
                killer.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BAD_OMEN, 36000, 0));
            }
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.3D, 0.5D, 0.3D, 0.05D);
            if (NetherExp.STATUE.isPresent()) {
                Entity statue = NetherExp.STATUE.get().create(serverLevel);
                if (statue instanceof StatueEntity statueEntity) {
                    statueEntity.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                    serverLevel.addFreshEntity(statueEntity);
                }
            }
        }
        super.die(cause);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            if (this.hurtTime > 0 && !this.entityData.get(IS_PROTECTED)) return event.setAndContinue(RawAnimation.begin().thenPlay("hurt"));
            if (this.isSick()) return event.setAndContinue(RawAnimation.begin().thenLoop("sick"));

            // Если молится Азазелю
            if (this.entityData.get(IS_PRAYING)) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("pray_sit").thenLoop("pray_loop"));
            }

            if (event.isMoving()) {
                if (this.getDeltaMovement().horizontalDistanceSqr() > 0.015) return event.setAndContinue(RawAnimation.begin().thenLoop("run"));
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }
    @Nullable @Override protected SoundEvent getAmbientSound() { return this.isSick() ? null : SoundEvents.PILLAGER_AMBIENT; }
    @Nullable @Override protected SoundEvent getHurtSound(DamageSource damageSourceIn) { return SoundEvents.PILLAGER_HURT; }
    @Nullable @Override protected SoundEvent getDeathSound() { return SoundEvents.PILLAGER_DEATH; }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsSick", this.isSick());
        tag.putInt("ProtectedTimer", this.protectedTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSick(tag.getBoolean("IsSick"));
        this.protectedTimer = tag.getInt("ProtectedTimer");
    }
}