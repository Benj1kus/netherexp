package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class AzazelEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Boolean> IS_AGGRO = SynchedEntityData.defineId(AzazelEntity.class, EntityDataSerializers.BOOLEAN);

    // 0 = Idle, 1 = Wind, 2 = Defence, 3 = Wheel, 4 = Arrow, 5 = Pray
    public static final EntityDataAccessor<Integer> ATTACK_STATE = SynchedEntityData.defineId(AzazelEntity.class, EntityDataSerializers.INT);
    // 0 = Normal, 1 = Damaged (<50%), 2 = Low HP (<25%)
    public static final EntityDataAccessor<Integer> PHASE_STATE = SynchedEntityData.defineId(AzazelEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(Component.literal("The Divine Chariot Azazel"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);

    private int hitCounter = 0;
    private int attackTimer = 0;
    private boolean playedPraySound = false;

    private int arrowAttackVariant = 0;

    public AzazelEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 800.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_AGGRO, false);
        this.entityData.define(ATTACK_STATE, 0);
        this.entityData.define(PHASE_STATE, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AzazelMoveGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 64.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.entityData.get(IS_AGGRO)) this.bossEvent.addPlayer(player);
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

    // --- ЛОГИКА УРОНА И АГРЕССИИ ---
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;

        if (this.entityData.get(ATTACK_STATE) == 2) {
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.5F);
            return false;
        }

        if (!this.entityData.get(IS_AGGRO)) {
            triggerAggro();
        }

        if (source.getEntity() instanceof LivingEntity) {
            this.hitCounter++;
            if (this.hitCounter >= 10 && this.entityData.get(ATTACK_STATE) == 0) {
                this.hitCounter = 0;
                startDefenseStun();
            }
        }

        return super.hurt(source, amount);
    }

    private void triggerAggro() {
        this.entityData.set(IS_AGGRO, true);
        this.playSound(ModSounds.AZAZEL_IDLE_4.get(), 1.5F, 1.0F);

        for (ServerPlayer player : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D))) {
            this.bossEvent.addPlayer(player);
            Component title = Component.literal("DON'T BE AFRAID").withStyle(ChatFormatting.RED);
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));
        }
    }

    // --- СТАРТЕРЫ АТАК ---
    private void startDefenseStun() {
        this.entityData.set(ATTACK_STATE, 2);
        this.attackTimer = 100;
        this.heal(20.0F);
        this.playSound(ModSounds.DEFENCE.get(), 1.0F, 1.0F);
    }

    private void startWindAttack() {
        this.entityData.set(ATTACK_STATE, 1);
        this.attackTimer = 60;
        this.playSound(SoundEvents.PHANTOM_SWOOP, 2.0F, 0.5F);
    }

    private void startWheelAttack() {
        this.entityData.set(ATTACK_STATE, 3);
        this.attackTimer = 90;
        this.playSound(ModSounds.WHEEL_ATTACK.get(), 1.0F, 1.0F);
    }

    private void startPrayAttack() {
        this.entityData.set(ATTACK_STATE, 5);
        this.attackTimer = 180;
        this.playSound(ModSounds.AZAZEL_PRAY.get(), 1.0F, 1.0F);

        List<BelieverEntity> believers = this.level().getEntitiesOfClass(BelieverEntity.class, this.getBoundingBox().inflate(40.0D));
        for (BelieverEntity believer : believers) {
            believer.setProtected(150);
        }
    }

    private void startArrowAttack() {
        this.entityData.set(ATTACK_STATE, 4);
        this.attackTimer = 120;
        this.arrowAttackVariant = this.random.nextInt(3);
        this.playSound(ModSounds.ARROW_ATTACK.get(), 1.0F, 1.0F);
    }

    // --- ГЛАВНЫЙ ЦИКЛ ---
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            boolean isAggro = this.entityData.get(IS_AGGRO);
            int attackState = this.entityData.get(ATTACK_STATE);
            int currentPhase = this.entityData.get(PHASE_STATE);

            if (this.tickCount % 20 == 0) {
                int wingRand = this.random.nextInt(3);
                SoundEvent wingSound = wingRand == 0 ? ModSounds.WING_1.get() : (wingRand == 1 ? ModSounds.WING_2.get() : ModSounds.WING_3.get());
                this.playSound(wingSound, 1.0F, this.getVoicePitch());
            }

            float healthPct = this.getHealth() / this.getMaxHealth();
            if (healthPct <= 0.25F && currentPhase < 2) {
                this.entityData.set(PHASE_STATE, 2);
                this.playSound(ModSounds.AZAZEL_PHASE.get(), 1.0F, 1.0F);
            } else if (healthPct <= 0.50F && currentPhase < 1) {
                this.entityData.set(PHASE_STATE, 1);
                this.playSound(ModSounds.AZAZEL_PHASE.get(), 1.0F, 1.0F);
            }

            // 1. НЕЙТРАЛЬНАЯ ФАЗА
            if (!isAggro) {
                if (this.tickCount % 240 == 0) {
                    this.playSound(ModSounds.IDLE_PRAY.get(), 1.0F, 1.0F);
                }

                List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(30.0D));
                for (Player p : nearbyPlayers) {
                    if (p.hasEffect(MobEffects.BAD_OMEN)) {
                        triggerAggro();
                        break;
                    }
                }
            }
            // 2. АГРЕССИВНАЯ ФАЗА
            else {
                // АУРА ТРЕВОГИ: Каждую секунду накладываем эффект Anxiety (музыка!) на игроков в радиусе 100 блоков
                if (this.tickCount % 20 == 0) {
                    List<Player> auraPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(100.0D));
                    for (Player p : auraPlayers) {
                        // 100 тиков (5 секунд). Иконка включена (true), партиклы зелья отключены (false).
                        p.addEffect(new MobEffectInstance(NetherExp.ANXIETY_EFFECT.get(), 100, 0, false, false, true));
                    }
                }

                if (attackState > 0) {
                    this.attackTimer--;

                    if (attackState == 1) performWindAttack();
                    else if (attackState == 3) performWheelAttack();
                    else if (attackState == 4) performArrowAttack();

                    if (this.attackTimer <= 0) this.entityData.set(ATTACK_STATE, 0);
                } else {
                    if (this.getTarget() != null && this.random.nextInt(80) == 0) {
                        int randAtk = this.random.nextInt(4);
                        if (randAtk == 0) startWindAttack();
                        else if (randAtk == 1) startWheelAttack();
                        else if (randAtk == 2) startArrowAttack();
                        else startPrayAttack();
                    }
                    handlePassiveSummons();
                }
            }
        }
    }

    // --- ЛОГИКА АТАК ---
    private void performWindAttack() {
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(20.0D));
        for (Player player : players) {
            Vec3 knockbackVec = player.position().subtract(this.position()).normalize().scale(0.8D);
            player.setDeltaMovement(player.getDeltaMovement().add(knockbackVec.x, 0.2D, knockbackVec.z));
            player.hurtMarked = true;

            if (this.attackTimer == 30) player.hurt(this.damageSources().mobAttack(this), 5.0F);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            double radius = 3.0 + Math.sin(this.tickCount * 0.5) * 2.0;
            double angle = this.tickCount * 0.4;
            serverLevel.sendParticles(ParticleTypes.FLAME, this.getX() + Math.cos(angle) * radius, this.getY() + 2.0, this.getZ() + Math.sin(angle) * radius, 5, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private void performWheelAttack() {
        if (this.attackTimer <= 50 && this.getTarget() != null) {
            LivingEntity target = this.getTarget();

            Vec3 dashVec = target.position().subtract(this.position()).normalize().scale(1.2D);
            this.setDeltaMovement(dashVec.x, this.getDeltaMovement().y, dashVec.z);

            if (this.tickCount % 3 == 0) {
                List<Player> hitPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(1.0D));
                for (Player player : hitPlayers) {
                    player.hurt(this.damageSources().mobAttack(this), 4.0F);
                    player.setDeltaMovement(dashVec.scale(1.5D));
                    player.hurtMarked = true;
                }
            }
        }
    }

    private void performArrowAttack() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        if (this.arrowAttackVariant == 0 && this.attackTimer == 100) {
            for (int i = 0; i < 30; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 10.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 10.0;
                Arrow arrow = new Arrow(this.level(), target.getX() + offsetX, target.getY() + 10.0, target.getZ() + offsetZ);
                arrow.setDeltaMovement(0, -1.5D, 0);
                this.level().addFreshEntity(arrow);
            }
        }
        else if (this.arrowAttackVariant == 1 &&
                (this.attackTimer == 100 || this.attackTimer == 80 || this.attackTimer == 60)) {

            for (int i = 0; i < 20; i++) {

                double offsetX = (this.random.nextDouble() - 0.5D) * 2.0D;
                double offsetZ = (this.random.nextDouble() - 0.5D) * 2.0D;

                EvokerFangs fangs = new EvokerFangs(
                        this.level(),
                        target.getX() + offsetX,
                        target.getY(),
                        target.getZ() + offsetZ,
                        this.random.nextFloat() * 360F,
                        0,
                        this
                );

                this.level().addFreshEntity(fangs);
            }
        }
        else if (this.arrowAttackVariant == 2 && this.attackTimer == 100) {
            if (this.level() instanceof ServerLevel serverLevel && NetherExp.LASER.isPresent()) {
                double[][] offsets = {{5, 5}, {-5, 5}, {5, -5}, {-5, -5}};
                for (double[] offset : offsets) {
                    LaserEntity laser = NetherExp.LASER.get().create(serverLevel);
                    if (laser != null) {
                        laser.setPos(this.getX() + offset[0], this.getY(), this.getZ() + offset[1]);
                        serverLevel.addFreshEntity(laser);
                    }
                }
            }
        }
    }

    private void handlePassiveSummons() {
        if (this.random.nextInt(600) == 0 && this.level() instanceof ServerLevel serverLevel) {
            this.playSound(ModSounds.SPAWN_UNIT.get(), 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.0D, this.getZ(), 30, 1.5, 1.5, 1.5, 0.05);

            boolean spawnBossUnits = this.random.nextBoolean();

            if (spawnBossUnits && NetherExp.STATUE_BOSSUNIT.isPresent()) {
                for (int i = 0; i < 2; i++) {
                    StatueBossunitEntity unit = NetherExp.STATUE_BOSSUNIT.get().create(serverLevel);
                    if (unit != null) {
                        unit.setPos(this.getX() + (this.random.nextDouble() - 0.5) * 10, this.getY(), this.getZ() + (this.random.nextDouble() - 0.5) * 10);
                        serverLevel.addFreshEntity(unit);
                    }
                }
            } else if (!spawnBossUnits && NetherExp.STATUE.isPresent()) {
                for (int i = 0; i < 4; i++) {
                    StatueEntity statue = NetherExp.STATUE.get().create(serverLevel);
                    if (statue != null) {
                        statue.setPos(this.getX() + (this.random.nextDouble() - 0.5) * 10, this.getY(), this.getZ() + (this.random.nextDouble() - 0.5) * 10);
                        serverLevel.addFreshEntity(statue);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.entityData.get(IS_AGGRO)) {
            if (this.entityData.get(PHASE_STATE) == 2) {
                return ModSounds.BREATH_AZAZEL.get();
            }
            int rand = this.random.nextInt(3);
            return rand == 0 ? ModSounds.AZAZEL_IDLE_1.get() : (rand == 1 ? ModSounds.AZAZEL_IDLE_2.get() : ModSounds.AZAZEL_IDLE_3.get());
        }
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return this.random.nextBoolean() ? ModSounds.AZAZEL_DAMAGE_1.get() : ModSounds.AZAZEL_DAMAGE_2.get();
    }

    // --- СОХРАНЕНИЕ ---
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsAggro", this.entityData.get(IS_AGGRO));
        tag.putInt("HitCounter", this.hitCounter);
        tag.putBoolean("PlayedPraySound", this.playedPraySound);
        tag.putInt("PhaseState", this.entityData.get(PHASE_STATE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("IsAggro")) this.entityData.set(IS_AGGRO, tag.getBoolean("IsAggro"));
        this.hitCounter = tag.getInt("HitCounter");
        if (tag.contains("PlayedPraySound")) this.playedPraySound = tag.getBoolean("PlayedPraySound");
        if (tag.contains("PhaseState")) this.entityData.set(PHASE_STATE, tag.getInt("PhaseState"));
    }

    class AzazelMoveGoal extends Goal {
        private final AzazelEntity azazel;

        public AzazelMoveGoal(AzazelEntity azazel) {
            this.azazel = azazel;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return azazel.entityData.get(IS_AGGRO) && azazel.entityData.get(ATTACK_STATE) == 0 && azazel.getTarget() != null;
        }

        @Override
        public void tick() {
            LivingEntity target = azazel.getTarget();
            if (target != null) {
                if (azazel.distanceToSqr(target) > 400.0D) azazel.getNavigation().moveTo(target, 1.0D);
                else azazel.getNavigation().stop();
            }
        }
    }

    // --- АНИМАЦИИ ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            boolean isAggro = this.entityData.get(IS_AGGRO);
            int state = this.entityData.get(ATTACK_STATE);

            if (!isAggro) return event.setAndContinue(RawAnimation.begin().thenLoop("idle_pray"));

            if (state == 1) return event.setAndContinue(RawAnimation.begin().thenPlay("wind_attack"));
            if (state == 2) return event.setAndContinue(RawAnimation.begin().thenPlay("defence_stun"));
            if (state == 3) return event.setAndContinue(RawAnimation.begin().thenPlay("wheel"));
            if (state == 4) return event.setAndContinue(RawAnimation.begin().thenPlay("arrow_attack"));
            if (state == 5) return event.setAndContinue(RawAnimation.begin().thenPlay("pray"));

            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}