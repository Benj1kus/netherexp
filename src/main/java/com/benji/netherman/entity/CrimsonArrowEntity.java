package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CrimsonArrowEntity extends AbstractArrow {
    private int bouncesLeft;

    public CrimsonArrowEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setPierceLevel((byte) 20);
    }

    public CrimsonArrowEntity(Level level, LivingEntity shooter) {
        super(NetherExp.CRIMSON_ARROW_ENTITY.get(), shooter, level);
        this.setPierceLevel((byte) 20);
        this.bouncesLeft = level.random.nextInt(6) + 5;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.inGround && this.bouncesLeft > 0) {
            if (this.tickCount % 2 == 0) {
                DustParticleOptions redstone = new DustParticleOptions(new Vector3f(0.75F, 0.0F, 0.1F), 1.0F);
                this.level().addParticle(redstone, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.bouncesLeft <= 0) {
            super.onHitBlock(result);
            return;
        }

        this.bouncesLeft--;

        Level level = this.level();
        Vec3 motion = this.getDeltaMovement();
        Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
        double dotProduct = motion.dot(normal);
        Vec3 reflectedMotion = motion.subtract(normal.scale(2.0 * dotProduct));
        this.setDeltaMovement(reflectedMotion.scale(0.95D));
        double horizontalDist = reflectedMotion.horizontalDistance();
        this.setYRot((float) (Mth.atan2(reflectedMotion.x, reflectedMotion.z) * (180F / Math.PI)));
        this.setXRot((float) (Mth.atan2(reflectedMotion.y, horizontalDist) * (180F / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        level.playSound(null, this.blockPosition(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.NEUTRAL, 1.0F, 1.5F);
        level.playSound(null, this.blockPosition(), SoundEvents.ARROW_HIT, SoundSource.NEUTRAL, 0.8F, 2.0F);
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            DustParticleOptions hitDust = new DustParticleOptions(new Vector3f(0.8F, 0.0F, 0.0F), 1.5F);
            serverLevel.sendParticles(hitDust, result.getLocation().x, result.getLocation().y, result.getLocation().z, 15, 0.1, 0.1, 0.1, 0.05);
        }
        this.inGround = false;
        this.inGroundTime = 0;
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(NetherExp.CRIMSON_ARROW_ITEM.get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BouncesLeft", this.bouncesLeft);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.bouncesLeft = tag.getInt("BouncesLeft");
    }
}