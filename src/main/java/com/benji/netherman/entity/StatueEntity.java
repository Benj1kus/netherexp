package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class StatueEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public boolean isFrozen = false;
    public boolean hasAttacked = false; // Флаг, чтобы ударить только один раз

    public StatueEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D) // Очень быстрая, когда ее не видят
                .add(Attributes.ATTACK_DAMAGE, 10.0D) // 10 урона за один удар
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D); // Вообще не откидывается
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Кастомная атака, которая уважает "заморозку" и правило "одного удара"
        this.goalSelector.addGoal(1, new StatueAttackGoal(this, 1.0D, false));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // --- ЛОГИКА "ПЛАЧУЩЕГО АНГЕЛА" ---
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            boolean isLookedAtNow = isBeingLookedAt();

            if (isLookedAtNow) {
                if (!this.isFrozen) {
                    // ИГРОК ТОЛЬКО ЧТО ПОВЕРНУЛСЯ И УВИДЕЛ СТАТУЮ!
                    this.isFrozen = true;
                    this.getNavigation().stop(); // Резко тормозим

                    // Сбрасываем флаг атаки. Теперь, когда игрок отвернется, она снова сможет ударить
                    this.hasAttacked = false;

                    // 1. Проигрываем случайный звук пружины
                    int rand = this.random.nextInt(3);
                    SoundEvent springSound = rand == 0 ? ModSounds.SPRING_1.get() : (rand == 1 ? ModSounds.SPRING_2.get() : ModSounds.SPRING_3.get());
                    this.playSound(springSound, 1.0F, 1.0F);

                    // 2. Отправляем триггер на клиент для анимации approach
                    this.triggerAnim("action_controller", "approach");
                }

                // Пока на статую смотрят - жестко обнуляем движение
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            } else {
                this.isFrozen = false; // Игрок отвернулся, можно бежать!
            }
        }
    }

    // Математика обзора: проверяем, смотрит ли хоть один игрок в сторону статуи
    private boolean isBeingLookedAt() {
        for (Player player : this.level().players()) {
            if (!player.isSpectator() && player.isAlive()) {
                Vec3 viewVector = player.getViewVector(1.0F).normalize();
                Vec3 toStatue = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
                double dist = toStatue.length();

                if (dist < 64.0D) { // Статуя реагирует на взгляд в радиусе 64 блоков
                    toStatue = toStatue.normalize();
                    double dotProduct = viewVector.dot(toStatue);

                    // dotProduct > 0.1 означает, что статуя находится в конусе зрения игрока
                    if (dotProduct > 0.1D) {
                        // Учитываем препятствия (стены). Если между игроком и статуей блок - он ее не видит
                        if (player.hasLineOfSight(this)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // --- ЗВУКИ УРОНА ---
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        int rand = this.random.nextInt(3);
        return rand == 0 ? ModSounds.STATUE_HURT_1.get() : (rand == 1 ? ModSounds.STATUE_HURT_2.get() : ModSounds.STATUE_HURT_3.get());
    }

    // --- АНИМАЦИИ ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Контроллер 1: Вечный IDLE
        controllers.add(new AnimationController<>(this, "base_controller", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));

        // Контроллер 2: Триггерная анимация APPROACH (Срабатывает только при команде с сервера)
        AnimationController<StatueEntity> actionController = new AnimationController<>(this, "action_controller", 0, event -> PlayState.STOP);
        actionController.triggerableAnim("approach", RawAnimation.begin().thenPlay("approach"));
        controllers.add(actionController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    // --- КАСТОМНАЯ ЦЕЛЬ ДЛЯ АТАКИ ---
    // Позволяет ударить только если НЕ заморожена и еще НЕ била в этот подход
    class StatueAttackGoal extends MeleeAttackGoal {
        public StatueAttackGoal(StatueEntity mob, double speed, boolean followingTargetEvenIfNotSeen) {
            super(mob, speed, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            StatueEntity statue = (StatueEntity) this.mob;
            return !statue.isFrozen && !statue.hasAttacked && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            StatueEntity statue = (StatueEntity) this.mob;
            return !statue.isFrozen && !statue.hasAttacked && super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
            double reach = this.getAttackReachSqr(enemy);
            if (distToEnemySqr <= reach && this.getTicksUntilNextAttack() <= 0) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(enemy);

                // Статуя нанесла удар и "успокоилась". Ждет, пока на нее посмотрят и отвернутся.
                ((StatueEntity) this.mob).hasAttacked = true;
            }
        }
    }
}