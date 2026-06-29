package com.benji.netherman.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AzazelSpikesProjectileEntity extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(AzazelSpikesProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PIVOT_ID = SynchedEntityData.defineId(AzazelSpikesProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ORBIT_RADIUS = SynchedEntityData.defineId(AzazelSpikesProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORBIT_ANGLE = SynchedEntityData.defineId(AzazelSpikesProjectileEntity.class, EntityDataSerializers.FLOAT);

    private int lifeTicks = 0;
    private float currentAngle = -1.0F;

    public AzazelSpikesProjectileEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); 
        this.noPhysics = true;   
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(MODE, 0);
        this.entityData.define(PIVOT_ID, -1);
        this.entityData.define(ORBIT_RADIUS, 0.0F);
        this.entityData.define(ORBIT_ANGLE, 0.0F);
    }

    public void setMode(int mode) { this.entityData.set(MODE, mode); }
    public void setPivotId(int id) { this.entityData.set(PIVOT_ID, id); }
    public void setOrbitRadius(float radius) { this.entityData.set(ORBIT_RADIUS, radius); }
    public void setOrbitAngle(float angle) { this.entityData.set(ORBIT_ANGLE, angle); }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (lifeTicks > 200) {
            spawnDisappearParticles();
            this.discard();
            return;
        }

        
        
        if (!this.level().isClientSide()) {
            for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(0.2D))) {
                if (!player.isInvulnerable()) {
                    this.onHitEntity(new EntityHitResult(player));
                    break; 
                }
            }
        }

        
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hitresult);
        }

        
        int mode = this.entityData.get(MODE);

        if (mode == 1) {
            
            if (currentAngle == -1.0F) currentAngle = this.entityData.get(ORBIT_ANGLE);

            LivingEntity pivot = (LivingEntity) this.level().getEntity(this.entityData.get(PIVOT_ID));
            if (pivot != null && pivot.isAlive()) {
                currentAngle += 1.5F;

                double rad = Math.toRadians(currentAngle);
                float radius = this.entityData.get(ORBIT_RADIUS);

                double targetX = pivot.getX() + Math.cos(rad) * radius;
                double targetZ = pivot.getZ() + Math.sin(rad) * radius;
                double targetY = pivot.getY() + 0.5D;

                this.setPos(targetX, targetY, targetZ);
                this.setYRot(currentAngle + 90.0F);
            } else if (!this.level().isClientSide()) {
                this.discard();
            }
        } else {
            
            Vec3 move = this.getDeltaMovement();
            this.setPos(this.getX() + move.x, this.getY() + move.y, this.getZ() + move.z);

            if (move.lengthSqr() > 0.001D) {
                this.setYRot((float)(Math.atan2(move.z, move.x) * (180F / Math.PI)) - 90.0F);
            }
        }

        if (this.level() instanceof ServerLevel sl && this.tickCount % 2 == 0) {
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 0.5D, this.getZ(), 1, 0.2D, 0.2D, 0.2D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide() && result.getEntity() instanceof Player player) {
            
            if (player.isInvulnerable()) return;

            player.hurt(this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), 5.0F);
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1)); 

            spawnDisappearParticles();
            this.discard(); 
        }
    }

    private void spawnDisappearParticles() {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 15, 0.5D, 0.5D, 0.5D, 0.1D);
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY(), this.getZ(), 15, 0.5D, 0.5D, 0.5D, 0.1D);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}