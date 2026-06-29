package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinPrisonerEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(PiglinPrisonerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MASTER_ID = SynchedEntityData.defineId(PiglinPrisonerEntity.class, EntityDataSerializers.INT);

    public static final int STATE_IDLE_PRISON = 0;
    public static final int STATE_MINING = 1;
    public static final int STATE_FREEDOM = 2;
    public static final int STATE_IDLE = 3;
    public static final int STATE_GIVE = 4;
    public static final int STATE_TRANSFORM = 5;

    private int actionTimer = 0;
    private int miningSoundTimer = 0;

    public PiglinPrisonerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, STATE_IDLE_PRISON);
        this.entityData.define(MASTER_ID, -1);
    }

    public int getEntityState() { return this.entityData.get(STATE); }
    public void setEntityState(int state) { this.entityData.set(STATE, state); }

    public int getMasterId() { return this.entityData.get(MASTER_ID); }
    public void setMasterId(int id) { this.entityData.set(MASTER_ID, id); }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }

    @Override
    public void tick() {
        super.tick();

        int state = this.getEntityState();
        Entity master = this.level().getEntity(this.getMasterId());

        if (this.level().isClientSide()) {
            if ((state == STATE_IDLE_PRISON || state == STATE_MINING) && master != null) {

                if (this.tickCount % 40 == 0) {
                    Vec3 start = this.position().add(0, 1.0, 0);
                    Vec3 end = master.position().add(0, 1.0, 0);
                    double distance = start.distanceTo(end);

                    Vec3 dir = end.subtract(start).normalize();

                    for (double d = 0; d < distance; d += 1.0) {
                        Vec3 pos = start.add(dir.scale(d));
                        this.level().addParticle(DustParticleOptions.REDSTONE, pos.x, pos.y, pos.z, 0, 0, 0);
                    }
                }
            }
            return;
        }

        this.getNavigation().stop();
        if (state == STATE_IDLE_PRISON || state == STATE_MINING) {
            if (master == null || !master.isAlive()) {
                this.getNavigation().stop();
                this.setEntityState(STATE_FREEDOM);
                this.actionTimer = 20;
                return;
            }

            BlockPos myPos = this.blockPosition();
            BlockPos targetMiningPos = null;

            outerLoop:
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = 0; y <= 1; y++) {
                        if (x == 0 && z == 0 && y == 0) continue;

                        BlockPos checkPos = myPos.offset(x, y, z);
                        Block block = this.level().getBlockState(checkPos).getBlock();

                        if (block == Blocks.STONE || block == Blocks.BLACKSTONE ||
                                block == Blocks.NETHER_GOLD_ORE || block == Blocks.NETHER_QUARTZ_ORE ||
                                block == Blocks.GILDED_BLACKSTONE) {
                            targetMiningPos = checkPos;
                            break outerLoop;
                        }
                    }
                }
            }

            if (targetMiningPos != null) {
                if (state != STATE_MINING) this.setEntityState(STATE_MINING);
                this.getNavigation().stop();

                double dx = targetMiningPos.getX() + 0.5 - this.getX();
                double dz = targetMiningPos.getZ() + 0.5 - this.getZ();
                float targetRot = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;

                this.setYRot(targetRot);
                this.yHeadRot = targetRot;
                this.yBodyRot = targetRot;
                if (this.miningSoundTimer <= 0) {
                    int randSound = this.random.nextInt(4);
                    switch (randSound) {
                        case 0 -> { this.playSound(ModSounds.PRISON_1.get(), 1.0F, this.getVoicePitch()); this.miningSoundTimer = 17; } 
                        case 1 -> { this.playSound(ModSounds.PRISON_2.get(), 1.0F, this.getVoicePitch()); this.miningSoundTimer = 38; } 
                        case 2 -> { this.playSound(ModSounds.PRISON_3.get(), 1.0F, this.getVoicePitch()); this.miningSoundTimer = 14; } 
                        case 3 -> { this.playSound(ModSounds.PRISON_4.get(), 1.0F, this.getVoicePitch()); this.miningSoundTimer = 19; } 
                    }
                } else {
                    this.miningSoundTimer--;
                }

            } else {
                if (state != STATE_IDLE_PRISON) this.setEntityState(STATE_IDLE_PRISON);
                this.miningSoundTimer = 0;

                if (this.distanceToSqr(master) > 25.0D) {
                    this.getNavigation().moveTo(master, 1.0D);
                } else {
                    this.getNavigation().stop();
                }
            }
        }
        else if (state == STATE_FREEDOM) {
            this.getNavigation().stop();
            if (this.actionTimer > 0) this.actionTimer--;
            else this.setEntityState(STATE_IDLE);
        }
        else if (state == STATE_IDLE) {
            this.getNavigation().stop();
            Player nearestPlayer = this.level().getNearestPlayer(this, 3.0D);
            if (nearestPlayer != null) {
                this.getLookControl().setLookAt(nearestPlayer);
                this.setEntityState(STATE_GIVE);
                this.actionTimer = 10;
            }
        }
        else if (state == STATE_GIVE) {
            this.getNavigation().stop();
            if (this.actionTimer > 0) {
                this.actionTimer--;
            } else {
                int rand = this.random.nextInt(100);
                ItemStack reward = new ItemStack(rand < 60 ? Items.GOLD_INGOT : (rand < 90 ? Items.ENDER_PEARL : Items.NETHERITE_INGOT));
                this.spawnAtLocation(reward);
                this.playSound(SoundEvents.PIGLIN_CELEBRATE, 1.0F, this.getVoicePitch());

                this.setEntityState(STATE_TRANSFORM);
                this.actionTimer = 15;
            }
        }
        else if (state == STATE_TRANSFORM) {
            this.getNavigation().stop();
            if (this.actionTimer > 0) {
                this.actionTimer--;
            } else {
                Piglin piglin = EntityType.PIGLIN.create(this.level());
                if (piglin != null) {
                    piglin.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                    this.level().addFreshEntity(piglin);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
                    }
                }
                this.discard();
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            return switch (this.getEntityState()) {
                case STATE_MINING -> event.setAndContinue(RawAnimation.begin().thenLoop("mining"));
                case STATE_FREEDOM -> event.setAndContinue(RawAnimation.begin().thenPlay("freedom"));
                case STATE_GIVE -> event.setAndContinue(RawAnimation.begin().thenPlay("give"));
                case STATE_TRANSFORM -> event.setAndContinue(RawAnimation.begin().thenPlay("transform"));
                case STATE_IDLE -> event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
                default -> event.setAndContinue(RawAnimation.begin().thenLoop("idle_prison"));
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MasterId", this.getMasterId());
        tag.putInt("PrisonState", this.getEntityState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setMasterId(tag.getInt("MasterId"));
        this.setEntityState(tag.getInt("PrisonState"));
    }
}