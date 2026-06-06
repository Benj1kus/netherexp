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
        // Задаем сквозное пробитие мобов по умолчанию, чтобы стрела летела сквозь них
        this.setPierceLevel((byte) 20);
    }

    public CrimsonArrowEntity(Level level, LivingEntity shooter) {
        super(NetherExp.CRIMSON_ARROW_ENTITY.get(), shooter, level);
        this.setPierceLevel((byte) 20);
        // Рандомим количество рикошетов от 2 до 5 при создании стрелы
        this.bouncesLeft = level.random.nextInt(6) + 5;
    }

    @Override
    public void tick() {
        super.tick();

        // Спавн частиц редстоуна во время полета, если стрела еще способна рикошетить
        if (this.level().isClientSide && !this.inGround && this.bouncesLeft > 0) {
            if (this.tickCount % 2 == 0) {
                // Создаем багряный цвет пыли (RGB: 0.75, 0.0, 0.1)
                DustParticleOptions redstone = new DustParticleOptions(new Vector3f(0.75F, 0.0F, 0.1F), 1.0F);
                this.level().addParticle(redstone, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Если лимит рикошетов исчерпан — стрела застревает в блоке согласно ванильной логике
        if (this.bouncesLeft <= 0) {
            super.onHitBlock(result);
            return;
        }

        this.bouncesLeft--;

        Level level = this.level();
        Vec3 motion = this.getDeltaMovement();

        // Получаем вектор нормали плоскости столкновения блока
        Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());

        // Формула упругого отражения: V_reflected = V - 2 * (V * Normal) * Normal
        double dotProduct = motion.dot(normal);
        Vec3 reflectedMotion = motion.subtract(normal.scale(2.0 * dotProduct));

        // Слегка сохраняем кинетическую энергию, компенсируя трение воздуха (множитель 0.95D)
        this.setDeltaMovement(reflectedMotion.scale(0.95D));

        // Корректируем визуальный разворот модели стрелы в пространстве под новый вектор движения
        double horizontalDist = reflectedMotion.horizontalDistance();
        this.setYRot((float) (Mth.atan2(reflectedMotion.x, reflectedMotion.z) * (180F / Math.PI)));
        this.setXRot((float) (Mth.atan2(reflectedMotion.y, horizontalDist) * (180F / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        // Пружинистый желеобразный звук отскока и шлепок слизи
        level.playSound(null, this.blockPosition(), SoundEvents.SLIME_BLOCK_FALL, SoundSource.NEUTRAL, 1.0F, 1.5F);
        level.playSound(null, this.blockPosition(), SoundEvents.ARROW_HIT, SoundSource.NEUTRAL, 0.8F, 2.0F);

        // Взрыв багряных частиц на месте удара о блок
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            DustParticleOptions hitDust = new DustParticleOptions(new Vector3f(0.8F, 0.0F, 0.0F), 1.5F);
            serverLevel.sendParticles(hitDust, result.getLocation().x, result.getLocation().y, result.getLocation().z, 15, 0.1, 0.1, 0.1, 0.05);
        }

        // Принудительно сообщаем движку, что стрела не должна переходить в спящий режим внутри блока
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