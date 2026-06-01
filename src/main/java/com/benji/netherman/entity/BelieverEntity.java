package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
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

public class BelieverEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Синхронизируемый параметр для болезни
    private static final EntityDataAccessor<Boolean> IS_SICK =
            SynchedEntityData.defineId(BelieverEntity.class, EntityDataSerializers.BOOLEAN);

    public BelieverEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // ХП как у жителя
                .add(Attributes.MOVEMENT_SPEED, 0.25D); // Базовая скорость ходьбы
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_SICK, false);
    }

    public boolean isSick() { return this.entityData.get(IS_SICK); }
    public void setSick(boolean sick) { this.entityData.set(IS_SICK, sick); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PanicGoal заставляет моба быстро убегать при получении урона. 1.5D - множитель скорости для бега
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    // --- ВЗАИМОДЕЙСТВИЕ (ПКМ) ---
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1. Если кликают ПОРОХОМ и он ЗДОРОВ
        if (stack.is(Items.GUNPOWDER) && !this.isSick()) {
            if (!player.isCreative()) stack.shrink(1);
            this.setSick(true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // 2. Если кликают ЗОЛОТЫМ ЯБЛОКОМ и он БОЛЕН
        if (stack.is(Items.GOLDEN_APPLE) && this.isSick()) {
            if (!player.isCreative()) stack.shrink(1);
            this.setSick(false);

            // Звук довольного жителя
            this.playSound(SoundEvents.PILLAGER_CELEBRATE, 1.0F, 1.0F);

            // Зеленые частицы лечения (только на сервере отправляем пакет клиентам)
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        15, 0.3, 0.3, 0.3, 0.0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    // --- ЛОГИКА ОБЕЗДВИЖИВАНИЯ ---
    @Override
    public void travel(Vec3 travelVector) {
        // Если болен, полностью обнуляем вектор движения и не даем ему поворачиваться
        if (this.isSick()) {
            this.getNavigation().stop(); // Сбрасываем маршрут
            super.travel(Vec3.ZERO);
        } else {
            super.travel(travelVector);
        }
    }

    // --- АНИМАЦИИ ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {

            // 1. Анимация получения урона (приоритет)
            if (this.hurtTime > 0) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("hurt"));
            }

            // 2. Анимация болезни
            if (this.isSick()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("sick"));
            }

            // 3. Анимация движения
            if (event.isMoving()) {
                // Если скорость движения больше обычной (значит он убегает в панике)
                if (this.getDeltaMovement().horizontalDistanceSqr() > 0.015) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("run"));
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }

            // 4. Ожидание
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    // --- СОХРАНЕНИЕ NBT ДАННЫХ ---
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsSick", this.isSick());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSick(tag.getBoolean("IsSick"));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }


    // --- ЗВУКИ ВАНИЛЬНОГО ЖИТЕЛЯ ---
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isSick() ? null : SoundEvents.PILLAGER_AMBIENT; // Если болен, он молчит
    }
    // --- ЛОГИКА ЧИХАНИЯ ---
    @Override
    public void tick() {
        super.tick();

        // Только на сервере, только если болен
        if (!this.level().isClientSide() && this.isSick()) {
            // Шанс примерно раз в 4-5 секунд (80 тиков)
            if (this.random.nextInt(80) == 0) {
                // Воспроизводим звук чихания. Убедись, что ModSounds.SNEEZE зарегистрирован!
                this.playSound(com.benji.netherman.ModSounds.SNEEZE.get(), 1.0F, this.getVoicePitch());
            }
        }
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // --- ДОБАВЛЕНО: Эффект Bad Omen для убийцы ---
            // Проверяем, был ли убийца игроком
            if (cause.getEntity() instanceof Player killer) {
                // 30 минут = 30 * 60 секунд * 20 тиков = 36000 тиков
                killer.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BAD_OMEN, 36000, 0));
            }

            // 1. Спавним темно-серый дым
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    30, 0.3D, 0.5D, 0.3D, 0.05D);

            // 2. Спавним Статую
            // Убедись, что твоя статуя зарегистрирована как NetherExp.STATUE.get()
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
}