package com.benji.netherman.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AzazelSplashEntity extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTicks = 0;

    public AzazelSplashEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true); 
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        if (lifeTicks > 200) {
            this.discard();
            return;
        }

        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS) {
            this.onHit(hitresult);
        }

        this.checkInsideBlocks();
        Vec3 move = this.getDeltaMovement();
        this.setPos(this.getX() + move.x, this.getY() + move.y, this.getZ() + move.z);

        
        
        if (move.lengthSqr() > 0.001D) {
            double d0 = move.horizontalDistance();
            this.setYRot((float)(Mth.atan2(move.x, move.z) * (180F / Math.PI)) + 90.0F);
            this.setXRot((float)(Mth.atan2(move.y, d0) * (180F / Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(), this.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof Player player) {

            float damage = com.benji.netherman.config.AzazelConfig.HUMAN_SPLASH_DAMAGE.get().floatValue();
            player.hurt(this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), damage);

            
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 30, 1.0, 1.0, 1.0, 0.2);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}