package com.benji.netherman.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class LaserEntity extends Mob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(LaserEntity.class, EntityDataSerializers.INT);

    private float moveAngle;

    public LaserEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(false);
        this.moveAngle = this.random.nextFloat() * 360.0F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LIFE_TICKS, 1200);
    }

    // --- ОТКЛЮЧЕНИЕ КОЛЛИЗИИ И ТОЛЧКОВ ---
    @Override
    public boolean isPushable() { return false; }
    @Override
    public void push(Entity entity) {}
    @Override
    protected void doPush(Entity entity) {}
    @Override
    public boolean isPickable() { return false; }
    @Override
    public void knockback(double strength, double x, double z) {}
    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @Override
    public void tick() {
        super.tick();

        int life = this.entityData.get(LIFE_TICKS);

        if (!this.level().isClientSide()) {
            life--;
            this.entityData.set(LIFE_TICKS, life);

            // ЗАПУСК ТВОЕЙ АНИМАЦИИ СМЕРТИ ЗА 1 СЕКУНДУ ДО ИСЧЕЗНОВЕНИЯ
            if (life == 20) {
                this.triggerAnim("controller", "death");
            }

            if (life <= 0 && this.tickCount > 1200) {
                this.discard();
                return;
            }

            if (this.tickCount % 10 == 0) {
                this.moveAngle += (this.random.nextFloat() - 0.5F) * 60.0F;
            }

            double speed = 0.15D;
            double forwardX = Math.cos(Math.toRadians(this.moveAngle)) * speed;
            double forwardZ = Math.sin(Math.toRadians(this.moveAngle)) * speed;

            double waveOffset = Math.sin(this.tickCount * 0.3) * 0.15D;
            double perpX = Math.cos(Math.toRadians(this.moveAngle + 90)) * waveOffset;
            double perpZ = Math.sin(Math.toRadians(this.moveAngle + 90)) * waveOffset;

            this.setPos(this.getX() + forwardX + perpX, this.getY(), this.getZ() + forwardZ + perpZ);

            if (this.tickCount % 2 == 0) {
                List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox());
                for (Player player : players) {
                    if (player.isAlive() && !player.isCreative()) {
                        player.hurt(this.damageSources().magic(), 10.0F);
                        player.setSecondsOnFire(3);
                    }
                }
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY(), this.getZ(),
                        2, 0.1D, 0.1D, 0.1D, 0.02D);
            }
        }
    }

    // --- АНИМАЦИИ ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<LaserEntity> controller = new AnimationController<>(this, "controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        });

        // Регистрируем триггер на анимацию death
        controller.triggerableAnim("death", RawAnimation.begin().thenPlay("death"));

        controllers.add(controller);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void checkDespawn() { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LifeTicks", this.entityData.get(LIFE_TICKS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(LIFE_TICKS, tag.getInt("LifeTicks"));
    }
}