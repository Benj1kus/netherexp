package com.benji.netherman.entity;

import com.benji.netherman.block.entity.GrandDoorBlockEntity;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.Mob;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.benji.netherman.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import net.minecraft.nbt.CompoundTag;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class
GuardianEntity extends Monster implements GeoEntity {
    private java.util.UUID ownerUUID = null;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(GuardianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_BUFFED = SynchedEntityData.defineId(GuardianEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GREETING_PHASE = SynchedEntityData.defineId(GuardianEntity.class, EntityDataSerializers.INT);
    private int greetingCooldown = 0;

    public void setOwnerUUID(java.util.UUID uuid) { this.ownerUUID = uuid; }
    public java.util.UUID getOwnerUUID() { return this.ownerUUID; }
    public static final int STATE_NEUTRAL = 0;
    public static final int STATE_ANGRY = 1;
    public static final int STATE_WALK = 2;
    public static final int STATE_MELEE = 3;
    public static final int STATE_ATTACK_CHANGE = 4;
    public static final int STATE_ATTACK_SPEC = 5;
    public static final int STATE_SPAWNING = 6;
    public static final int STATE_GREETING = 7;
    private int spawnTicks = 0;
    private int greetingTicks = 0;
    private int greetingLoops = 0;


    public GuardianEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, STATE_NEUTRAL);
        this.entityData.define(IS_BUFFED, false);
        this.entityData.define(GREETING_PHASE, 0);
    }

    public int getGreetingPhase() { return this.entityData.get(GREETING_PHASE); }
    public void setGreetingPhase(int phase) { this.entityData.set(GREETING_PHASE, phase); }
    public void startSpawning() {
        this.setEntityState(STATE_SPAWNING);
        this.spawnTicks = 65;
    }


    public boolean isBuffed() { return this.entityData.get(IS_BUFFED); }
    public void setBuffed(boolean buffed) { this.entityData.set(IS_BUFFED, buffed); }

    public int getEntityState() { return this.entityData.get(STATE); }
    public void setEntityState(int state) { this.entityData.set(STATE, state); }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getEntityState() == STATE_NEUTRAL) {
            int rand = this.random.nextInt(3);
            return rand == 0 ? ModSounds.GUARDIAN_NEUTRAL_1.get() : (rand == 1 ? ModSounds.GUARDIAN_NEUTRAL_2.get() : ModSounds.GUARDIAN_NEUTRAL_3.get());
        } else if (this.getEntityState() == STATE_WALK || this.getEntityState() == STATE_ANGRY) {
            int rand = this.random.nextInt(3);
            return rand == 0 ? ModSounds.GUARDIAN_IDLE_1.get() : (rand == 1 ? ModSounds.GUARDIAN_IDLE_2.get() : ModSounds.GUARDIAN_IDLE_3.get());
        }
        return null;
    }
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return this.random.nextBoolean() ? ModSounds.GUARDIAN_DAMAGE_1.get() : ModSounds.GUARDIAN_DAMAGE_2.get();
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(ModSounds.GUARDIAN_WALK.get(), 1.0F, 1.0F);
    }
    @Override
    public float getVoicePitch() {
        return 0.8F + this.random.nextFloat() * 0.4F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("GreetingCooldown", this.greetingCooldown);
        if (this.ownerUUID != null) {
            compound.putUUID("SummonerUUID", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.greetingCooldown = compound.getInt("GreetingCooldown");
        if (compound.hasUUID("SummonerUUID")) {
            this.ownerUUID = compound.getUUID("SummonerUUID");
        }
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide()) {
            AABB searchBox = this.getBoundingBox().inflate(30.0);
            List<GuardianEntity> guardians = this.level().getEntitiesOfClass(GuardianEntity.class, searchBox, e -> e != this && e.isAlive());

            for (GuardianEntity guardian : guardians) {
                guardian.playSound(ModSounds.WEAKNESS.get(), 5.0F, guardian.getVoicePitch());
            }
        }
        super.die(cause);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            if (this.getEntityState() == STATE_SPAWNING) {
                BlockState state = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
                for (int i = 0; i < 4; i++) {
                    double dx = (this.random.nextDouble() - 0.5) * 6.0;
                    double dz = (this.random.nextDouble() - 0.5) * 6.0;
                    this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                            this.getX() + dx, this.getY(), this.getZ() + dz,
                            0.0, 0.3, 0.0);
                }
            }
        } else {
            if (this.spawnTicks > 0) {
                if (this.spawnTicks == 65) {
                    this.playSound(SoundEvents.WARDEN_EMERGE, 1.0F, 1.0F);
                }

                this.spawnTicks--;
                if (this.spawnTicks == 0) {
                    this.setEntityState(STATE_NEUTRAL);
                }
                return;
            }
            if (this.tickCount % 10 == 0) {
                AABB searchBox = this.getBoundingBox().inflate(30.0);
                List<WelcomerEntity> welcomers = this.level().getEntitiesOfClass(WelcomerEntity.class, searchBox);

                boolean hasBuff = false;
                for (WelcomerEntity welcomer : welcomers) {
                    if (welcomer.isCasting()) {
                        hasBuff = true;
                        break;
                    }
                }
                this.setBuffed(hasBuff);

                if (this.getEntityState() == STATE_NEUTRAL && this.getTarget() == null && this.greetingCooldown == 0) {
                    List<GhastlyEntity> ghastlies = this.level().getEntitiesOfClass(GhastlyEntity.class, searchBox);

                    boolean hasTamedGhastly = false;
                    for (GhastlyEntity g : ghastlies) {
                        if (g.isTame()) {
                            hasTamedGhastly = true;
                            break;
                        }
                    }

                    if (hasTamedGhastly) {
                        this.setEntityState(STATE_GREETING);
                        this.setGreetingPhase(1);
                        this.greetingTicks = 20;
                        this.greetingLoops = 0;
                        this.greetingCooldown = 24000;
                    }
                }
            }
            if (this.getEntityState() == STATE_GREETING) {
                if (this.getTarget() != null) {
                    this.setEntityState(STATE_NEUTRAL);
                    this.setGreetingPhase(0);
                    return;
                }
                List<GhastlyEntity> ghastlies = this.level().getEntitiesOfClass(GhastlyEntity.class, this.getBoundingBox().inflate(15.0));
                for (GhastlyEntity g : ghastlies) {
                    if (g.isTame()) {
                        this.getLookControl().setLookAt(g, 30.0F, 30.0F);
                        this.setYRot(this.getYHeadRot());
                        this.yBodyRot = this.getYHeadRot();
                        break;
                    }
                }

                if (this.greetingTicks > 0) {
                    this.greetingTicks--;
                    if (this.getGreetingPhase() == 2 && this.greetingTicks == 59) {
                        SoundEvent roarSound = switch (this.greetingLoops) {
                            case 0 -> ModSounds.GUARDIAN_ROAR_1.get();
                            case 1 -> ModSounds.GUARDIAN_ROAR_2.get();
                            default -> {
                                if (!this.level().isClientSide()) {
                                    BlockPos center = this.blockPosition();
                                    BlockPos.betweenClosedStream(center.offset(-20, -20, -20), center.offset(20, 20, 20)).forEach(pos -> {
                                        if (this.level().getBlockEntity(pos) instanceof GrandDoorBlockEntity door) {
                                            if (door.bossCooldown <= 0) {
                                                door.togglePermanent();
                                            }
                                        }
                                    });
                                }
                                yield ModSounds.GUARDIAN_ROAR_3.get();
                            }
                        };
                        this.playSound(roarSound, 1.5F, this.getVoicePitch());
                    }
                    if (this.greetingTicks == 0) {
                        switch (this.getGreetingPhase()) {
                            case 1:
                                this.setGreetingPhase(2);
                                this.greetingTicks = 60;
                                break;
                            case 2:
                                this.setGreetingPhase(3);
                                this.greetingTicks = 20;
                                break;
                            case 3:
                                this.greetingLoops++;
                                if (this.greetingLoops >= 3) {
                                    this.setEntityState(STATE_NEUTRAL);
                                    this.setGreetingPhase(0);
                                } else {
                                    this.setGreetingPhase(4);
                                    this.greetingTicks = 20;
                                }
                                break;
                            case 4:
                                this.setGreetingPhase(1);
                                this.greetingTicks = 20;
                                break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 0, false, false, (entity) -> {
            if (this.ownerUUID == null) return false;
            if (entity instanceof Player) return false;
            if (entity instanceof GuardianEntity otherGuardian) {
                if (otherGuardian.getOwnerUUID() != null && otherGuardian.getOwnerUUID().equals(this.ownerUUID)) {
                    return false;
                }
            }
            return entity instanceof Enemy;
        }));

        this.goalSelector.addGoal(1, new GuardianBehaviorGoal(this));
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<GuardianEntity> controller = new AnimationController<>(this, "controller", 5, event -> {
            switch (this.getEntityState()) {
                case STATE_ANGRY: return event.setAndContinue(RawAnimation.begin().thenPlay("angry_mode"));
                case STATE_WALK: return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
                case STATE_SPAWNING: return event.setAndContinue(RawAnimation.begin().thenPlay("spawn"));
                case STATE_MELEE: return event.setAndContinue(RawAnimation.begin().thenLoop("attack_loop"));
                case STATE_ATTACK_CHANGE: return event.setAndContinue(RawAnimation.begin().thenPlay("attack_change"));
                case STATE_ATTACK_SPEC: return event.setAndContinue(RawAnimation.begin().thenPlay("attack_spec"));
                case STATE_GREETING:
                    return switch (this.getGreetingPhase()) {
                        case 1 -> event.setAndContinue(RawAnimation.begin().thenPlay("roar_open"));
                        case 2 -> event.setAndContinue(RawAnimation.begin().thenLoop("roar_loop"));
                        case 3 -> event.setAndContinue(RawAnimation.begin().thenPlay("roar_close"));
                        default -> event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
                    };
                default: return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
        });

        if (this.getEntityState() == STATE_SPAWNING) {
            controller.setAnimation(RawAnimation.begin().thenPlay("spawn"));
        }

        controllers.add(controller);
    }

    public void performMeleeAttack() {
        LivingEntity target = this.getTarget();
        double attackRadiusSq = this.isBuffed() ? 64.0 : 16.0;
        float damage = this.isBuffed() ? 8.0F : 3.0F;

        if (target != null && this.distanceToSqr(target) <= attackRadiusSq) {
            target.invulnerableTime = 0;
            target.hurt(this.damageSources().mobAttack(this), damage);
        }
    }

    public void performMegaPunch() {
        int rand = this.random.nextInt(3);
        SoundEvent specSound = rand == 0 ? ModSounds.SPEC_ATTACK_1.get() : (rand == 1 ? ModSounds.SPEC_ATTACK_2.get() : ModSounds.SPEC_ATTACK_3.get());
        this.playSound(specSound, 1.5F, 1.0F);

        double inflateSize = this.isBuffed() ? 20.0 : 10.0;
        double attackRadiusSq = this.isBuffed() ? 400.0 : 100.0;
        float damage = this.isBuffed() ? 15.0F : 10.0F;

        AABB hitBox = this.getBoundingBox().inflate(inflateSize);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(
                LivingEntity.class,
                hitBox,
                e -> e != this
                        && !(e instanceof WelcomerEntity)
                        && !(e instanceof ManipulatorEntity)
                        && !(e instanceof BelieverEntity)
                        && !(e instanceof GuardianEntity)
                        && !(e instanceof StatueBossunitEntity)
                        && !(e instanceof StatueEntity)
                        && !(e instanceof AzazelEntity)
        );

        for (LivingEntity e : targets) {
            if (this.distanceToSqr(e) <= attackRadiusSq) {
                e.hurt(this.damageSources().mobAttack(this), damage);
                e.setDeltaMovement(e.getDeltaMovement().add(0, 0.9, 0));
                e.hurtMarked = true;
            }
        }
        this.level().broadcastEntityEvent(this, (byte) 100);
    }
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 100) {
            for (int i = 0; i < 360; i += 10) {
                double rad = Math.toRadians(i);
                double x = Math.cos(rad);
                double z = Math.sin(rad);

                this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        this.getX(), this.getY() + 0.2, this.getZ(),
                        x * 0.7, 0.05, z * 0.7);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}