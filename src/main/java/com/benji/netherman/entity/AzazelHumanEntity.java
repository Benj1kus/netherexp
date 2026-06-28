package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.config.AzazelConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import software.bernie.geckolib.util.GeckoLibUtil;

public class AzazelHumanEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> BOSS_STATE = SynchedEntityData.defineId(AzazelHumanEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DIALOGUE_TICK = SynchedEntityData.defineId(AzazelHumanEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(Component.literal("Azazel, The Awakened"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);

    private static final int[] LINE_LENGTHS = {31, 33, 40, 31, 28, 33, 35, 39, 29, 29};

    public AzazelHumanEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BOSS_STATE, 0);
        this.entityData.define(DIALOGUE_TICK, 0);
    }

    // config
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType reason, @Nullable net.minecraft.world.entity.SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {

        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AzazelConfig.HUMAN_MAX_HEALTH.get());
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(AzazelConfig.HUMAN_MOVEMENT_SPEED.get());
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(AzazelConfig.HUMAN_KNOCKBACK_RESISTANCE.get());
        }
        this.setHealth(this.getMaxHealth());

        BlockPos doorPos = null;
        for (BlockPos p : BlockPos.betweenClosed(this.blockPosition().offset(-64, -20, -64), this.blockPosition().offset(64, 20, 64))) {
            if (level.getBlockState(p).is(NetherExp.MAZE_DOOR.get())) {
                doorPos = p;
                break;
            }
        }
        if (doorPos != null) {
            double dX = doorPos.getX() - this.getX();
            double dZ = doorPos.getZ() - this.getZ();
            float yaw = (float) (Mth.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
            this.setYRot(yaw);
            this.setYBodyRot(yaw);
            this.setYHeadRot(yaw);
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.entityData.get(BOSS_STATE) == 5) this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.entityData.get(BOSS_STATE) < 5) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        int state = this.entityData.get(BOSS_STATE);

        if (state == 0 && player.getItemInHand(hand).is(NetherExp.AZAZEL_TROPHY_ITEM.get())) {
            if (!player.isCreative()) player.getItemInHand(hand).shrink(1);
            this.entityData.set(BOSS_STATE, 1);
            this.entityData.set(DIALOGUE_TICK, 0);

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 4.0D, this.getZ(), 200, 2.0D, 4.0D, 2.0D, 0.05D);
                sl.playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.5F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (state == 3) {
            this.playSound(ModSounds.BREATH_AZAZEL.get(), 1.0F, 0.8F);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;
        int state = this.entityData.get(BOSS_STATE);

        if (state < 5) {
            if (state == 3 && source.getEntity() instanceof Player) {
                this.entityData.set(BOSS_STATE, 4);
                this.entityData.set(DIALOGUE_TICK, 0);
                this.playSound(ModSounds.LAUGH.get(), 2.0F, 1.0F);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            int state = this.entityData.get(BOSS_STATE);
            int tick = this.entityData.get(DIALOGUE_TICK);

            if (state >= 1 && state <= 4) {
                tick++;
                this.entityData.set(DIALOGUE_TICK, tick);

                if (state == 1 && tick >= 20) {
                    this.entityData.set(BOSS_STATE, 2);
                    this.entityData.set(DIALOGUE_TICK, 0);
                }
                else if (state == 2) {
                    int currentLine = Math.min(tick / 90, 9);
                    int lineTick = tick % 90;

                    int maxChars = LINE_LENGTHS[currentLine];
                    int charsVisible = lineTick / 2;

                    if (charsVisible <= maxChars && lineTick % 2 == 0) {
                        this.playSound(ModSounds.AZAZEL_VOICE.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                    }

                    if (lineTick == 0 && this.random.nextInt(3) == 0) {
                        SoundEvent[] sounds = {ModSounds.SPEECH_1.get(), ModSounds.SPEECH_2.get(), ModSounds.SPEECH_3.get(), ModSounds.SPEECH_4.get()};
                        this.playSound(sounds[this.random.nextInt(sounds.length)], 2.0F, 0.8F + this.random.nextFloat() * 0.4F);
                    }

                    if (tick >= 900) {
                        this.entityData.set(BOSS_STATE, 3);
                    }
                }
                else if (state == 4 && tick >= 30) {
                    this.entityData.set(BOSS_STATE, 5);
                    for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D))) {
                        this.bossEvent.addPlayer(p);
                    }
                }
            }

            if (state == 5 && this.tickCount % 20 == 0) {
                for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(100.0D))) {
                    p.addEffect(new MobEffectInstance(NetherExp.ANXIETY_EFFECT.get(), 300, 0, false, false, true));
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.entityData.get(BOSS_STATE);
            return switch (state) {
                case 0 -> event.setAndContinue(RawAnimation.begin().thenLoop("throme_headless"));
                case 1 -> event.setAndContinue(RawAnimation.begin().thenPlay("throme_mask"));
                case 2, 3 -> event.setAndContinue(RawAnimation.begin().thenLoop("throme_mask_idle"));
                case 4 -> event.setAndContinue(RawAnimation.begin().thenPlay("throme_stand"));
                default -> event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BossState", this.entityData.get(BOSS_STATE));
        tag.putInt("DialogueTick", this.entityData.get(DIALOGUE_TICK));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(BOSS_STATE, tag.getInt("BossState"));
        this.entityData.set(DIALOGUE_TICK, tag.getInt("DialogueTick"));

        if (!this.level().isClientSide() && this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AzazelConfig.HUMAN_MAX_HEALTH.get());
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(AzazelConfig.HUMAN_MOVEMENT_SPEED.get());
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(AzazelConfig.HUMAN_KNOCKBACK_RESISTANCE.get());
        }
    }
}