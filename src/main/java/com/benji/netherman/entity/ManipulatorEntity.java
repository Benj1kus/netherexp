package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.ModSounds; // ИМПОРТ ТВОИХ ЗВУКОВ
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions; // ИМПОРТ РЕДСТОУН ПАРТИКЛОВ
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ManipulatorEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(ManipulatorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEALTH_PHASE = SynchedEntityData.defineId(ManipulatorEntity.class, EntityDataSerializers.INT);

    public static final int STATE_IDLE = 0;
    public static final int STATE_WALK = 1;
    public static final int STATE_RUN = 2;
    public static final int STATE_ATTACK = 3;
    public static final int STATE_ATTACK_LOOP = 4;

    public int castTicks = 0;
    public int fleeTicks = 0;
    public int manipulationCooldown = 0;
    private boolean guardianSpawned = false;

    public ManipulatorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, STATE_IDLE);
        this.entityData.define(HEALTH_PHASE, 0);
    }

    public int getEntityState() { return this.entityData.get(STATE); }

    // ПЕРЕХВАТЫВАЕМ СМЕНУ СОСТОЯНИЯ ДЛЯ ЗВУКА И ПАРТИКЛОВ
    public void setEntityState(int state) {
        if (state == STATE_ATTACK && this.getEntityState() != STATE_ATTACK) {
            if (!this.level().isClientSide()) {
                // Триггерим клиентское событие №60 (Круг из партиклов)
                this.level().broadcastEntityEvent(this, (byte) 60);

                // Проигрываем рандомный звук SUMMON1 или SUMMON2
                SoundEvent summonSound = this.random.nextBoolean() ? ModSounds.SUMMON1.get() : ModSounds.SUMMON2.get();
                this.playSound(summonSound, 1.5F, this.getVoicePitch());
            }
        }
        this.entityData.set(STATE, state);
    }

    public int getHealthPhase() { return this.entityData.get(HEALTH_PHASE); }
    public void setHealthPhase(int phase) { this.entityData.set(HEALTH_PHASE, phase); }

    // ЗВУКИ ШАГОВ (Кряхтение костей с шансом 1 к 3)
    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (this.random.nextInt(3) == 0) {
            this.playSound(SoundEvents.WITHER_SKELETON_STEP, 0.5F, this.getVoicePitch());
        } else {
            super.playStepSound(pos, blockIn); // Обычный звук шага по блоку
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        if (this.manipulationCooldown > 0) {
            this.manipulationCooldown--;
        }

        if (this.castTicks > 0) {
            this.castTicks--;
            this.getNavigation().stop();

            if (this.castTicks == 0) {
                // Если мы кастовали просто атаку (скелеты/жители) - возвращаемся в IDLE
                if (this.getEntityState() == STATE_ATTACK) {
                    this.setEntityState(STATE_IDLE);
                }
                // Если мы кастовали луп (разум) - возвращаемся в IDLE
                else if (this.getEntityState() == STATE_ATTACK_LOOP) {
                    this.setEntityState(STATE_IDLE);
                }
            }
        }

        if (this.fleeTicks > 0) {
            this.fleeTicks--;
            if (this.fleeTicks == 0 && this.getEntityState() == STATE_RUN) {
                this.setEntityState(STATE_IDLE);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean flag = super.hurt(source, amount);
        if (flag && !this.level().isClientSide()) {
            float health = this.getHealth();

            if (health <= 40.0F && health > 20.0F && this.getHealthPhase() == 0) {
                this.setHealthPhase(1);
                triggerPhaseEffects();
            } else if (health <= 20.0F && health > 0.0F && this.getHealthPhase() == 1) {
                this.setHealthPhase(2);
                triggerPhaseEffects();
            }

            if (health <= 10.0F && !this.guardianSpawned) {
                this.guardianSpawned = true;
                this.setEntityState(STATE_ATTACK);
                this.castTicks = 35;
                spawnGuardianCompanion();
            }

            if (source.getEntity() instanceof Player && health > 10.0F) {
                this.fleeTicks = 60;
                this.setEntityState(STATE_RUN);
            }
        }
        return flag;
    }

    private void triggerPhaseEffects() {
        this.playSound(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 1.5F, 0.7F);
        for (int i = 0; i < 20; i++) {
            this.level().addParticle(ParticleTypes.DAMAGE_INDICATOR, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), 0, 0.1, 0);
        }
    }

    private void spawnGuardianCompanion() {
        BlockPos spawnPos = this.blockPosition().relative(this.getDirection().getOpposite(), 2);
        GuardianEntity guardian = NetherExp.GUARDIAN.get().create(this.level());
        if (guardian != null) {
            guardian.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, this.getYRot(), 0);

            if (this.getTarget() != null) {
                guardian.setTarget(this.getTarget());
            }

            guardian.startSpawning();
            this.level().addFreshEntity(guardian);
            // Звук убран отсюда, так как он теперь проигрывается автоматически при STATE_ATTACK
        }
    }

    public void spawnWitherSkeletons() {
        int count = this.random.nextInt(2) + 1;
        for (int i = 0; i < count; i++) {
            BlockPos p = this.blockPosition().offset(this.random.nextInt(7) - 3, 0, this.random.nextInt(7) - 3);
            WitherSkeleton skeleton = EntityType.WITHER_SKELETON.create(this.level());
            if (skeleton != null) {
                skeleton.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0, 0);

                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                skeleton.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
                skeleton.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
                skeleton.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));

                skeleton.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
                skeleton.setDropChance(EquipmentSlot.HEAD, 0.0f);
                skeleton.setDropChance(EquipmentSlot.CHEST, 0.0f);
                skeleton.setDropChance(EquipmentSlot.LEGS, 0.0f);
                skeleton.setDropChance(EquipmentSlot.FEET, 0.0f);

                if (this.getTarget() != null) {
                    skeleton.setTarget(this.getTarget());
                }

                this.level().addFreshEntity(skeleton);

                for(int j=0; j<8; j++) {
                    this.level().addParticle(ParticleTypes.CRIMSON_SPORE, skeleton.getRandomX(0.5), skeleton.getRandomY(), skeleton.getRandomZ(0.5), 0, 0.1, 0);
                }
            }
        }
        // Звук убран отсюда, так как он теперь проигрывается автоматически при STATE_ATTACK
    }

    // --- КЛИЕНТСКИЙ ОБРАБОТЧИК ПАРТИКЛОВ ПРИЗЫВА ---
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 60) {
            // Рисуем красивый круг вокруг моба
            for (int i = 0; i < 360; i += 10) {
                double rad = Math.toRadians(i);
                double x = Math.cos(rad) * 1.5; // Радиус 1.5 блока
                double z = Math.sin(rad) * 1.5;

                // Так как ванильный редстоун партикл часто игнорирует скорость,
                // мы спавним его немного "столбом", задавая случайную высоту, чтобы сымитировать взлет
                double yOffset = this.random.nextDouble() * 1.5;

                this.level().addParticle(DustParticleOptions.REDSTONE,
                        this.getX() + x, this.getY() + 0.1 + yOffset, this.getZ() + z,
                        0.0, 0.2, 0.0); // Передаем Y скорость на всякий случай
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // ПРИОРИТЕТ 1: Порабощение
        this.goalSelector.addGoal(1, new ManipulatorConvertGoal(this));
        // ПРИОРИТЕТ 2: Атака игрока
        this.goalSelector.addGoal(2, new ManipulatorAttackGoal(this));

        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.getEntityState();

            // 1. ПРИОРИТЕТ: Жесткие анимации атак (блокируем ходьбу во время каста)
            if (state == STATE_ATTACK) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("attack"));
            }
            if (state == STATE_ATTACK_LOOP) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("attack_loop"));
            }

            // 2. ВТОРОСТЕПЕННО: Анимации движения
            if (this.walkAnimation.isMoving()) {
                if (state == STATE_RUN) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("run"));
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }

            // 3. БАЗА: Простой
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}