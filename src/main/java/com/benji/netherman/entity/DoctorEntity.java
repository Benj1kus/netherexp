package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.stream.Collectors;

public class DoctorEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = Нет подсказки, 1 = doctor_hint, 2 = doctoradditional_hint, 3 = doctortrade_hint
    public static final EntityDataAccessor<Integer> HINT_STATE = SynchedEntityData.defineId(DoctorEntity.class, EntityDataSerializers.INT);
    public int hintTimer = 0;

    public DoctorEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D) // Вообще не ходит
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D); // 100% сопротивление отбрасыванию
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HINT_STATE, 0);
    }

    @Override
    protected void registerGoals() {
        // Убрали все цели на перемещение. Он только стоит и смотрит на игрока.
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    // Дополнительная защита от любых сдвигов (взрывы, удары)
    @Override
    public void knockback(double strength, double x, double z) {}
    @Override
    public void push(double x, double y, double z) {}

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) return InteractionResult.sidedSuccess(true);

        ItemStack stack = player.getItemInHand(hand);

        // 1. Сканируем радиус 10 блоков на наличие больных Believer
        List<BelieverEntity> nearbyBelievers = this.level().getEntitiesOfClass(BelieverEntity.class, this.getBoundingBox().inflate(10.0D));
        boolean hasSickBeliever = nearbyBelievers.stream().anyMatch(BelieverEntity::isSick);

        int currentState = this.entityData.get(HINT_STATE);

        if (hasSickBeliever) {
            // ЛОГИКА: ЕСТЬ БОЛЬНЫЕ
            if (currentState == 1) {
                // Если уже была первая подсказка -> показываем дополнительную
                this.entityData.set(HINT_STATE, 2);
            } else {
                // Показываем первую подсказку и дым
                this.entityData.set(HINT_STATE, 1);
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5D, this.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
            }
            this.hintTimer = 80; // Показываем на 4 секунды
            this.playSound(SoundEvents.VILLAGER_NO, 1.0F, this.getVoicePitch());

        } else {
            // ЛОГИКА: НЕТ БОЛЬНЫХ (МОЖНО ТОРГОВАТЬ)
            if (stack.is(Items.GOLDEN_APPLE)) {
                // ТОРГОВЛЯ!
                if (!player.isCreative()) stack.shrink(1);

                this.playSound(SoundEvents.VILLAGER_TRADE, 1.0F, this.getVoicePitch());
                this.giveRandomPotion(player);

                this.entityData.set(HINT_STATE, 3);
                this.hintTimer = 60;
            } else {
                // Игрок кликнул пустой рукой или другим предметом -> показываем подсказку торговли
                this.entityData.set(HINT_STATE, 3);
                this.hintTimer = 80;
                this.playSound(SoundEvents.VILLAGER_TRADE, 1.0F, this.getVoicePitch());
            }
        }

        return InteractionResult.sidedSuccess(false);
    }

    private void giveRandomPotion(Player player) {
        // Создаем базовое зелье
        ItemStack potion = new ItemStack(Items.POTION);

        // Получаем все зарегистрированные эффекты в игре
        List<MobEffect> allEffects = ForgeRegistries.MOB_EFFECTS.getValues().stream().toList();

        // Выбираем абсолютно случайный эффект
        MobEffect randomEffect = allEffects.get(this.random.nextInt(allEffects.size()));

        // Генерируем рандомную длительность (от 10 до 90 секунд) и уровень (от 1 до 4)
        int durationTicks = 200 + this.random.nextInt(1600);
        int amplifier = this.random.nextInt(4); // 0, 1, 2, или 3 (Level 1-4)

        // Применяем эффект к зелью
        PotionUtils.setCustomEffects(potion, List.of(new MobEffectInstance(randomEffect, durationTicks, amplifier)));

        // Выкидываем зелье в сторону игрока
        Vec3 dir = player.position().subtract(this.position()).normalize().scale(0.3);
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 1.0D, this.getZ(), potion);
        itemEntity.setDeltaMovement(dir.x, 0.3D, dir.z);
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.hintTimer > 0) {
            this.hintTimer--;
            if (this.hintTimer <= 0) {
                this.entityData.set(HINT_STATE, 0); // Прячем подсказку
            }
        }
    }

    // --- ЗВУКИ ---
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DOCTOR.get();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    // --- АНИМАЦИИ ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            // Доктор всегда стоит в idle
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}