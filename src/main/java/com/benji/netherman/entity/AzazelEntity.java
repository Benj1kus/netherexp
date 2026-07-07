package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.config.AzazelConfig;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.item.ItemStack;
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
    public static final EntityDataAccessor<Integer> ATTACK_STATE = SynchedEntityData.defineId(AzazelEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PHASE_STATE = SynchedEntityData.defineId(AzazelEntity.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<Integer> MERCY_TICK = SynchedEntityData.defineId(AzazelEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(Component.literal("The Divine Chariot Azazel"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);

    private final java.util.Map<java.util.UUID, Integer> midasProximityTracker = new java.util.HashMap<>();
    private final java.util.List<net.minecraft.core.BlockPos> prisonBlocks = new java.util.ArrayList<>();
    private net.minecraft.core.BlockPos prisonCenter = null;
    private Player prisonTarget = null;
    public boolean isInstantKill = false;

    private int hitCounter = 0;
    private int attackTimer = 0;
    private int nextShieldThreshold = -1;
    private boolean playedPraySound = false;

    private int arrowAttackVariant = 0;
    private int totalAttacksPerformed = 0;
    private boolean hasOfferedMercy = false;

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
        this.entityData.define(MERCY_TICK, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AzazelMoveGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 64.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType reason, @Nullable net.minecraft.world.entity.SpawnGroupData spawnData, @Nullable net.minecraft.nbt.CompoundTag dataTag) {

        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AzazelConfig.MAX_HEALTH.get());
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(AzazelConfig.MOVEMENT_SPEED.get());
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(AzazelConfig.KNOCKBACK_RESISTANCE.get());
        }

        this.setHealth(this.getMaxHealth());

        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
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
    public boolean addEffect(MobEffectInstance effect, @Nullable Entity source) {
        if (effect.getEffect() == NetherExp.MANIPULATION_EFFECT.get()) {
            return false;
        }
        return super.addEffect(effect, source);
    }

    @Override
    public void travel(Vec3 travelVector) {
        int attackState = this.entityData.get(ATTACK_STATE);
        if (attackState >= 6 || attackState == 2) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;

        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.FALLING_BLOCK) ||
                source.is(net.minecraft.world.damagesource.DamageTypes.CRAMMING)) {
            return false;
        }

        if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) {
            this.isInstantKill = true;
        }

        if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) {
            return false;
        }

        int attackState = this.entityData.get(ATTACK_STATE);

        if (attackState >= 7 && attackState <= 10) {
            return false;
        }

        if (attackState == 2) {
            this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.5F);
            return false;
        }

        if (attackState == 6) {
            this.entityData.set(ATTACK_STATE, 0);
            triggerAggro();
            return super.hurt(source, amount);
        }

        if (!this.entityData.get(IS_AGGRO)) {
            triggerAggro();
        }

        if (source.getEntity() instanceof LivingEntity) {

            if (this.nextShieldThreshold == -1) {
                int minInit = Math.min(AzazelConfig.SHIELD_HITS_MIN.get(), AzazelConfig.SHIELD_HITS_MAX.get());
                int maxInit = Math.max(AzazelConfig.SHIELD_HITS_MIN.get(), AzazelConfig.SHIELD_HITS_MAX.get());
                this.nextShieldThreshold = minInit + this.random.nextInt((maxInit - minInit) + 1);
            }

            this.hitCounter++;

            if (this.hitCounter >= this.nextShieldThreshold && attackState == 0) {
                this.hitCounter = 0;

                int minHits = Math.min(AzazelConfig.SHIELD_HITS_MIN.get(), AzazelConfig.SHIELD_HITS_MAX.get());
                int maxHits = Math.max(AzazelConfig.SHIELD_HITS_MIN.get(), AzazelConfig.SHIELD_HITS_MAX.get());
                this.nextShieldThreshold = minHits + this.random.nextInt((maxHits - minHits) + 1);

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
            Component title = Component.translatable("entity.netherman.azazel.mistake").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);

            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.empty()));
        }
    }
    private void startDefenseStun() {
        this.entityData.set(ATTACK_STATE, 2);
        this.attackTimer = 100;
        this.heal(20.0F);
        this.playSound(ModSounds.DEFENCE.get(), 1.0F, 1.0F);
    }

    private void startPullAttack() {
        this.entityData.set(ATTACK_STATE, 11);
        this.attackTimer = 60;
        this.playSound(SoundEvents.PHANTOM_SWOOP, 2.0F, 0.5F); 
    }

    private void startLaunchAttack() {
        this.entityData.set(ATTACK_STATE, 12);
        this.attackTimer = 80;
        this.playSound(ModSounds.AZAZEL_PRAY.get(), 1.0F, 1.5F); 
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
    private void startDeathCinematic() {
        if (this.level() instanceof ServerLevel sl) clearPrison(sl);
        this.entityData.set(ATTACK_STATE, 9);
        this.entityData.set(MERCY_TICK, 0);
        this.getNavigation().stop();
        this.setTarget(null);
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(100.0D));
        for (Player p : nearbyPlayers) {
            p.removeEffect(NetherExp.ANXIETY_EFFECT.get());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel sl) clearPrison(sl);
        super.remove(reason);
    }

    private void startMercyPhase() {
        this.entityData.set(ATTACK_STATE, 6);
        this.entityData.set(MERCY_TICK, 0);
        this.getNavigation().stop();
        this.setTarget(null);
    }

    @Override
    public void setHealth(float health) {
        int attackState = this.entityData.get(ATTACK_STATE);

        if (health <= this.getMaxHealth() * 0.05F && attackState < 9 && !this.isInstantKill) {
            super.setHealth(1.0F);
            this.removeAllEffects();
            startDeathCinematic();
            return;
        }

        super.setHealth(health);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.entityData.get(ATTACK_STATE) == 6) {
            ItemStack stack = player.getItemInHand(hand);
            net.minecraft.world.item.Item item = stack.getItem();

            boolean isWeapon = item == net.minecraft.world.item.Items.IRON_SWORD || item == net.minecraft.world.item.Items.DIAMOND_SWORD || item == net.minecraft.world.item.Items.NETHERITE_SWORD ||
                    item == net.minecraft.world.item.Items.IRON_AXE || item == net.minecraft.world.item.Items.DIAMOND_AXE || item == net.minecraft.world.item.Items.NETHERITE_AXE;

            if (isWeapon) {
                if (!player.isCreative()) stack.shrink(1);
                this.entityData.set(ATTACK_STATE, 7);
                this.entityData.set(MERCY_TICK, 0);

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 50, 0.5D, 0.5D, 0.5D, 0.05D);
                }
                this.playSound(SoundEvents.ITEM_BREAK, 1.0F, 0.5F);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    private void startArrowAttack() {
        this.entityData.set(ATTACK_STATE, 4);
        this.attackTimer = 120;
        this.arrowAttackVariant = this.random.nextInt(3);
        this.playSound(ModSounds.ARROW_ATTACK.get(), 1.0F, 1.0F);
    }
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            boolean isAggro = this.entityData.get(IS_AGGRO);
            int attackState = this.entityData.get(ATTACK_STATE);
            int currentPhase = this.entityData.get(PHASE_STATE);
            if (attackState >= 6 && attackState <= 10) {
                this.setTarget(null);
                int mercyTick = this.entityData.get(MERCY_TICK);
                mercyTick++;
                this.entityData.set(MERCY_TICK, mercyTick);
                if (attackState == 6) {
                    int textLen = 42;
                    int revealSpeed = 2;
                    int finishTextTick = textLen * revealSpeed;

                    if (mercyTick <= finishTextTick && mercyTick % revealSpeed == 0) {
                        this.playSound(ModSounds.AZAZEL_VOICE.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                    } else if (mercyTick == finishTextTick + 1) {
                        this.playSound(ModSounds.CLOCK.get(), 1.0F, 1.0F);
                    } else if (mercyTick > finishTextTick + 200) {
                        this.entityData.set(ATTACK_STATE, 0);
                        triggerAggro();
                        Player closestPlayer = this.level().getNearestPlayer(this, 30.0D);
                        if (closestPlayer != null) this.setTarget(closestPlayer);
                    }
                }
                else if (attackState == 7) {
                    int textLen = 28;
                    int revealSpeed = 2;
                    int finishTextTick = textLen * revealSpeed;

                    if (mercyTick <= finishTextTick && mercyTick % revealSpeed == 0) {
                        this.playSound(ModSounds.AZAZEL_VOICE.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                    }
                    else if (mercyTick == finishTextTick + 20) {
                        this.entityData.set(ATTACK_STATE, 8);
                        this.entityData.set(MERCY_TICK, 0);

                        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(100.0D));
                        for (Player p : nearbyPlayers) {
                            p.removeEffect(NetherExp.ANXIETY_EFFECT.get());
                        }
                    }
                }
                else if (attackState == 8) {
                    List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(30.0D));
                    for (Player p : nearbyPlayers) {
                        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, this.position().add(0, this.getEyeHeight(), 0));
                    }

                    if (mercyTick >= 40) {
                        this.playSound(ModSounds.FLASH.get(), 1.0F, 1.0F);
                        if (this.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 2.0D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                        this.bossEvent.removeAllPlayers();
                        this.discard();
                    }
                }
                else if (attackState == 9) {
                    int textLen = 56;
                    int revealSpeed = 2;

                    if (mercyTick == 1) {
                        this.playSound(ModSounds.AZAZEL_IDLE_4.get(), 1.5F, 1.0F);
                    } else if (mercyTick == 40) {
                        this.playSound(ModSounds.BREATH_AZAZEL.get(), 1.5F, 1.0F);
                    }

                    if (mercyTick <= textLen * revealSpeed && mercyTick % revealSpeed == 0) {
                        this.playSound(ModSounds.AZAZEL_VOICE.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                    }

                    if (mercyTick >= 200) {
                        if (this.level() instanceof ServerLevel serverLevel) {
                            serverLevel.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 4.0F, 1.0F);
                            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 1, 0, 0, 0, 0);
                            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 1.0D, 1.0D, 1.0D, 0.1D);

                            for (int i = 0; i < 30; i++) {
                                net.minecraft.world.item.Item goldItem = this.random.nextBoolean() ? net.minecraft.world.item.Items.GOLD_INGOT : net.minecraft.world.item.Items.GOLD_NUGGET;
                                net.minecraft.world.entity.item.ItemEntity gold = new net.minecraft.world.entity.item.ItemEntity(
                                        serverLevel, this.getX(), this.getY() + 1.0D, this.getZ(), new ItemStack(goldItem, 1)
                                );
                                gold.setDeltaMovement((this.random.nextDouble() - 0.5D) * 0.5D, 0.5D + this.random.nextDouble() * 0.3D, (this.random.nextDouble() - 0.5D) * 0.5D);
                                serverLevel.addFreshEntity(gold);
                            }

                            net.minecraft.core.BlockPos barrelPos = this.blockPosition();
                            serverLevel.setBlockAndUpdate(barrelPos, net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState());

                            net.minecraft.world.level.block.entity.BlockEntity blockEntity = serverLevel.getBlockEntity(barrelPos);
                            if (blockEntity instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity barrel) {
                                java.util.List<Integer> availableSlots = new java.util.ArrayList<>();
                                for (int i = 0; i < 27; i++) availableSlots.add(i);
                                java.util.Collections.shuffle(availableSlots);

                                ItemStack[] loot = new ItemStack[] {
                                        new ItemStack(NetherExp.MANIPULATOR_STICK.get(), 1),
                                        new ItemStack(NetherExp.MUSIC_DISC_AZAZEL.get(), 1),
                                        new ItemStack(NetherExp.MUSIC_DISC_BOSS.get(), 1),
                                        new ItemStack(NetherExp.CHANCE_TOTEM.get(), 2),
                                        new ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING, 1),
                                        new ItemStack(NetherExp.NOTE.get(), 1),
                                        new ItemStack(NetherExp.AZAZEL_TROPHY_ITEM.get(), 1),
                                        new ItemStack(net.minecraft.world.item.Items.DIAMOND, 25),
                                        new ItemStack(net.minecraft.world.item.Items.NETHERITE_SCRAP, 12),
                                        new ItemStack(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE, 4),
                                        new ItemStack(NetherExp.NETHER_SPAWNER_ITEM.get(), 1)
                                };

                                for (int i = 0; i < loot.length && i < availableSlots.size(); i++) {
                                    barrel.setItem(availableSlots.get(i), loot[i]);
                                }
                            }
                        }
                        this.bossEvent.removeAllPlayers();
                        this.discard();
                    }
                }
                else if (attackState == 10) {
                    if (mercyTick == 1) {
                        this.playSound(ModSounds.SPAWN_UNIT.get(), 1.0F, 1.0F);
                    }

                    if (mercyTick >= 40) {
                        this.entityData.set(ATTACK_STATE, 0);
                        this.entityData.set(MERCY_TICK, 0);
                    }
                }

                return;
            }


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
            else {
                if (this.tickCount % 20 == 0) {
                    List<Player> auraPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(100.0D));
                    for (Player p : auraPlayers) {
                        p.addEffect(new MobEffectInstance(NetherExp.ANXIETY_EFFECT.get(), 300, 0, false, false, true));
                    }
                }

                if (attackState > 0) {
                    this.attackTimer--;

                    if (attackState == 1) performWindAttack();
                    else if (attackState == 3) performWheelAttack();
                    else if (attackState == 4) performArrowAttack();
                    else if (attackState == 11) performPullAttack();
                    else if (attackState == 12) performLaunchAttack();
                    else if (attackState == 13) performMidasAttack();
                    else if (attackState == 14) performPrisonAttack();

                    if (this.attackTimer <= 0) this.entityData.set(ATTACK_STATE, 0);
                } else {
                    if (this.getTarget() != null && this.random.nextInt(AzazelConfig.ATTACK_CHANCE.get()) == 0) {
                        if (!this.hasOfferedMercy && this.totalAttacksPerformed >= 5) {
                            startMercyPhase();
                            this.hasOfferedMercy = true;
                        } else {
                            boolean hasClosePlayers = !this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(AzazelConfig.MELEE_ATTACK_RADIUS.get())).isEmpty();
                            boolean hasBelievers = !this.level().getEntitiesOfClass(BelieverEntity.class, this.getBoundingBox().inflate(40.0D)).isEmpty();

                            if (hasClosePlayers && this.random.nextInt(100) < AzazelConfig.MELEE_ATTACK_CHANCE.get()) {
                                if (this.random.nextBoolean()) {
                                    startLaunchAttack();
                                } else {
                                    startMidasAttack();
                                }
                            } else {
                                java.util.List<Integer> availableAttacks = new java.util.ArrayList<>();
                                availableAttacks.add(0); 
                                availableAttacks.add(1); 
                                availableAttacks.add(2); 
                                availableAttacks.add(3); 
                                availableAttacks.add(5); 

                                if (hasBelievers) {
                                    availableAttacks.add(4);
                                }

                                int choice = availableAttacks.get(this.random.nextInt(availableAttacks.size()));

                                if (choice == 0) startWindAttack();
                                else if (choice == 1) startWheelAttack();
                                else if (choice == 2) startArrowAttack();
                                else if (choice == 3) startPullAttack();
                                else if (choice == 4) startPrayAttack();
                                else if (choice == 5) startPrisonAttack();
                            }

                            this.totalAttacksPerformed++;
                        }
                    }
                    handlePassiveSummons();
                }
            }
        }
    }

    private void performLaunchAttack() {

        if (this.attackTimer == 40) {
            List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0D));

            for (Player player : players) {
                player.hurt(this.damageSources().mobAttack(this), AzazelConfig.LAUNCH_ATTACK_DAMAGE.get().floatValue());
                player.setDeltaMovement(player.getDeltaMovement().x, 2.5D, player.getDeltaMovement().z);
                player.hurtMarked = true;
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 2, 0.5D, 0.5D, 0.5D, 0.1D);
                    serverLevel.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.2F);
                }
            }
        }
    }

    private void startPrisonAttack() {
        this.prisonTarget = this.getTarget() instanceof Player ? (Player) this.getTarget() : this.level().getNearestPlayer(this, 40.0D);
        if (this.prisonTarget == null) return;

        this.entityData.set(ATTACK_STATE, 14);
        this.attackTimer = AzazelConfig.PRISON_DURATION.get();
        this.prisonCenter = this.prisonTarget.blockPosition();
        this.prisonBlocks.clear();
        this.playSound(ModSounds.AZAZEL_IDLE_4.get(), 1.5F, 1.0F);
    }

    private void performPrisonAttack() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.prisonCenter == null) return;

        if (this.attackTimer <= 0) {
            clearPrison(serverLevel);
            return;
        }

        int maxTime = AzazelConfig.PRISON_DURATION.get();
        int elapsed = maxTime - this.attackTimer;

        if (elapsed >= 10 && elapsed <= 50 && elapsed % 4 == 0) {
            int step = (elapsed - 10) / 4;
            int radius = AzazelConfig.PRISON_RADIUS.get();
            int configMaxHeight = AzazelConfig.PRISON_MAX_HEIGHT.get();

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.round(Math.sqrt(x * x + z * z)) == radius) {
                        int maxH = Math.max(3, configMaxHeight - (Math.abs(x * 31 + z * 17) % 4));
                        int currentYOffset = step;

                        if (currentYOffset <= maxH) {
                            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(this.prisonCenter.getX() + x, this.prisonCenter.getY() + currentYOffset, this.prisonCenter.getZ() + z);

                            if (serverLevel.getBlockState(pos).canBeReplaced()) {
                                serverLevel.setBlockAndUpdate(pos, NetherExp.BLACKSTONE_COLUMN.get().defaultBlockState());
                                this.prisonBlocks.add(pos);
                                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.02);
                            }
                        }
                    }
                }
            }
        }

        if (elapsed > 50 && this.prisonTarget != null) {
            int highestBlockY = getHighestPrisonBlock();
            int radius = AzazelConfig.PRISON_RADIUS.get();

            if (this.prisonTarget.getY() + 1 > highestBlockY) {
                serverLevel.playSound(null, this.prisonCenter, SoundEvents.WITHER_BREAK_BLOCK, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.5F);

                for (int yOffset = 1; yOffset <= 5; yOffset++) {
                    int buildY = highestBlockY + yOffset;
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (Math.round(Math.sqrt(x * x + z * z)) == radius) {
                                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(this.prisonCenter.getX() + x, buildY, this.prisonCenter.getZ() + z);
                                if (serverLevel.getBlockState(pos).canBeReplaced()) {
                                    serverLevel.setBlockAndUpdate(pos, NetherExp.GRAND_DOOR_PART.get().defaultBlockState());
                                    this.prisonBlocks.add(pos);
                                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void clearPrison(ServerLevel level) {
        for (net.minecraft.core.BlockPos pos : this.prisonBlocks) {
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);

            if (state.getBlock() == NetherExp.BLACKSTONE_COLUMN.get() || state.getBlock() == NetherExp.GRAND_DOOR_PART.get()) {

                level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);

                if (state.getBlock() == NetherExp.BLACKSTONE_COLUMN.get()) {
                    level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(state));
                }
            }
        }
        this.prisonBlocks.clear();
    }

    private int getHighestPrisonBlock() {
        if (this.prisonBlocks.isEmpty()) return this.prisonCenter != null ? this.prisonCenter.getY() : 0;
        int highest = this.prisonBlocks.get(0).getY();
        for (net.minecraft.core.BlockPos pos : this.prisonBlocks) {
            if (pos.getY() > highest) highest = pos.getY();
        }
        return highest;
    }

    private void startMidasAttack() {
        this.entityData.set(ATTACK_STATE, 13);
        this.attackTimer = 120;
        this.midasProximityTracker.clear();
        this.playSound(SoundEvents.EVOKER_PREPARE_WOLOLO, 2.0F, 1.0F);
    }

    private void performMidasAttack() {
        if (this.level() instanceof ServerLevel serverLevel) {
            int maxTime = 120;
            int currentAttackTick = maxTime - this.attackTimer;

            if (this.attackTimer == 110) {
                boolean spawnStatues = this.random.nextBoolean();

                if (spawnStatues && NetherExp.STATUE_BOSSUNIT.isPresent()) {
                    int bossUnitCount = AzazelConfig.MIDAS_BOSSUNIT_COUNT.get();
                    for (int i = 0; i < bossUnitCount; i++) {
                        double angle = (2 * Math.PI / Math.max(1, bossUnitCount)) * i;
                        double offsetX = Math.cos(angle) * 4.0D;
                        double offsetZ = Math.sin(angle) * 4.0D;
                        StatueBossunitEntity unit = NetherExp.STATUE_BOSSUNIT.get().create(serverLevel);
                        if (unit != null) {
                            unit.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
                            serverLevel.addFreshEntity(unit);
                        }
                    }
                } else if (!spawnStatues && NetherExp.GUARDIAN.isPresent()) {
                    LivingEntity currentTarget = this.getTarget();
                    if (currentTarget == null) currentTarget = serverLevel.getNearestPlayer(this, 40.0D);

                    int guardianCount = AzazelConfig.MIDAS_GUARDIAN_COUNT.get();
                    for (int i = 0; i < guardianCount; i++) {
                        double angle = (2 * Math.PI / Math.max(1, guardianCount)) * i;
                        double offsetX = Math.cos(angle) * 4.0D;
                        double offsetZ = Math.sin(angle) * 4.0D;

                        GuardianEntity guardian = NetherExp.GUARDIAN.get().create(serverLevel);
                        if (guardian != null) {
                            guardian.moveTo(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ, this.getYRot(), 0);
                            if (currentTarget != null) guardian.setTarget(currentTarget);
                            guardian.startSpawning();
                            serverLevel.addFreshEntity(guardian);
                        }
                    }
                }
            }

            double radius = (currentAttackTick / (double) maxTime) * 30.0D;
            if (radius > 0) {
                for (int i = 0; i < 50; i++) {
                    double angle = this.random.nextDouble() * 2 * Math.PI;
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;
                    serverLevel.sendParticles(ParticleTypes.FLAME, this.getX() + offsetX, this.getY() + 0.2D, this.getZ() + offsetZ, 2, 0.1D, 0.1D, 0.1D, 0.05D);
                }
            }

            List<Player> allPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(35.0D));
            for (Player player : allPlayers) {
                double dist = player.distanceTo(this);

                if (Math.abs(dist - radius) <= 1.5D) {
                    player.hurt(this.damageSources().inFire(), AzazelConfig.MIDAS_FIRE_DAMAGE.get().floatValue());
                    player.setSecondsOnFire(5);
                }

                if (dist <= 6.0D) {
                    java.util.UUID uuid = player.getUUID();
                    int ticks = this.midasProximityTracker.getOrDefault(uuid, 0) + 1;

                    if (ticks >= AzazelConfig.MIDAS_GOLD_TIME.get()) {
                        turnRandomItemToGold(player);
                        ticks = 0;
                        serverLevel.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 1.0D, player.getZ(), 15, 0.5D, 0.5D, 0.5D, 0.1D);
                        serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
                    }
                    this.midasProximityTracker.put(uuid, ticks);
                } else {
                    this.midasProximityTracker.put(player.getUUID(), 0);
                }
            }
        }
    }

    private void turnRandomItemToGold(Player player) {
        java.util.List<Integer> validSlots = new java.util.ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() != net.minecraft.world.item.Items.RAW_GOLD) {
                validSlots.add(i);
            }
        }

        if (!validSlots.isEmpty()) {
            int randomSlot = validSlots.get(this.random.nextInt(validSlots.size()));
            ItemStack oldStack = player.getInventory().getItem(randomSlot);
            int count = oldStack.getCount();
            player.getInventory().setItem(randomSlot, new ItemStack(net.minecraft.world.item.Items.RAW_GOLD, count));
        }
    }


    private void performPullAttack() {
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(20.0D));

        for (Player player : players) {
            if (this.attackTimer > 15) {
                Vec3 pullVec = this.position().subtract(player.position()).normalize().scale(0.6D);
                player.setDeltaMovement(player.getDeltaMovement().add(pullVec.x, 0.0D, pullVec.z));
                player.hurtMarked = true;
            } else if (this.attackTimer == 15) {
                player.setDeltaMovement(player.getDeltaMovement().x, 1.5D, player.getDeltaMovement().z);
                player.hurtMarked = true;
                player.hurt(this.damageSources().mobAttack(this), AzazelConfig.PULL_ATTACK_DAMAGE.get().floatValue());
            }
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 5; i++) {
                double radius = 4.0D;
                double angle = this.random.nextDouble() * 2 * Math.PI;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double motionX = -offsetX * 0.1;
                double motionY = (this.random.nextDouble() - 0.5) * 0.1;
                double motionZ = -offsetZ * 0.1;

                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX() + offsetX, this.getY() + 1.0D, this.getZ() + offsetZ,
                        0,
                        motionX, motionY, motionZ, 1.0);
            }
        }
    }


    private void performWindAttack() {
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(20.0D));
        for (Player player : players) {
            Vec3 knockbackVec = player.position().subtract(this.position()).normalize().scale(0.8D);
            player.setDeltaMovement(player.getDeltaMovement().add(knockbackVec.x, 0.2D, knockbackVec.z));
            player.hurtMarked = true;

            if (this.attackTimer == 30) player.hurt(this.damageSources().mobAttack(this), AzazelConfig.WIND_ATTACK_DAMAGE.get().floatValue());
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
                    player.hurt(this.damageSources().mobAttack(this), AzazelConfig.WHEEL_ATTACK_DAMAGE.get().floatValue());
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

                int laserCount = 10;
                double radius = 7.0D;

                for (int i = 0; i < laserCount; i++) {
                    double angle = 2.0 * Math.PI * i / laserCount;
                    double offsetX = Math.cos(angle) * radius;
                    double offsetZ = Math.sin(angle) * radius;

                    LaserEntity laser = NetherExp.LASER.get().create(serverLevel);
                    if (laser != null) {
                        laser.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
                        serverLevel.addFreshEntity(laser);
                    }
                }
            }
        }
    }

    private void handlePassiveSummons() {
        if (this.random.nextInt(AzazelConfig.PASSIVE_SUMMON_CHANCE.get()) == 0 && this.level() instanceof ServerLevel serverLevel) {
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

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
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
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsAggro", this.entityData.get(IS_AGGRO));
        tag.putInt("HitCounter", this.hitCounter);
        tag.putBoolean("PlayedPraySound", this.playedPraySound);
        tag.putInt("MercyTick", this.entityData.get(MERCY_TICK));
        tag.putInt("PhaseState", this.entityData.get(PHASE_STATE));
        tag.putBoolean("HasOfferedMercy", this.hasOfferedMercy);
        tag.putInt("TotalAttacks", this.totalAttacksPerformed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("IsAggro")) this.entityData.set(IS_AGGRO, tag.getBoolean("IsAggro"));
        this.hitCounter = tag.getInt("HitCounter");
        if (tag.contains("PlayedPraySound")) this.playedPraySound = tag.getBoolean("PlayedPraySound");
        if (tag.contains("MercyTick")) this.entityData.set(MERCY_TICK, tag.getInt("MercyTick"));
        if (tag.contains("TotalAttacks")) this.totalAttacksPerformed = tag.getInt("TotalAttacks");
        if (tag.contains("HasOfferedMercy")) this.hasOfferedMercy = tag.getBoolean("HasOfferedMercy");
        if (tag.contains("PhaseState")) this.entityData.set(PHASE_STATE, tag.getInt("PhaseState"));

        if (!this.level().isClientSide() && this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AzazelConfig.MAX_HEALTH.get());
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(AzazelConfig.MOVEMENT_SPEED.get());
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(AzazelConfig.KNOCKBACK_RESISTANCE.get());
        }
    }


    class AzazelMoveGoal extends Goal {
        private final AzazelEntity azazel;

        public AzazelMoveGoal(AzazelEntity azazel) {
            this.azazel = azazel;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            int state = azazel.entityData.get(ATTACK_STATE);
            if (state >= 6 && state <= 10) return false;

            return azazel.entityData.get(IS_AGGRO) && state == 0 && azazel.getTarget() != null;
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

    private static final RawAnimation IDLE_PRAY_ANIM = RawAnimation.begin().thenLoop("idle_pray");
    private static final RawAnimation WIND_ATTACK_ANIM = RawAnimation.begin().thenPlay("wind_attack");
    private static final RawAnimation DEFENCE_STUN_ANIM = RawAnimation.begin().thenPlay("defence_stun");
    private static final RawAnimation WHEEL_ANIM = RawAnimation.begin().thenPlay("wheel");
    private static final RawAnimation ARROW_ATTACK_ANIM = RawAnimation.begin().thenPlay("arrow_attack");
    private static final RawAnimation PRAY_ANIM = RawAnimation.begin().thenPlay("pray");
    private static final RawAnimation CINEMATIC_MERCY_ANIM = RawAnimation.begin().thenPlay("cinematic_mercy");
    private static final RawAnimation CINEMATIC_DEATH_ANIM = RawAnimation.begin().thenPlay("cinematic_death");
    private static final RawAnimation CINEMATIC_SPAWN_ANIM = RawAnimation.begin().thenPlay("cinematic_spawn");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            boolean isAggro = this.entityData.get(IS_AGGRO);
            int state = this.entityData.get(ATTACK_STATE);

            if (!isAggro) return event.setAndContinue(IDLE_PRAY_ANIM);

            if (state == 1 || state == 11) return event.setAndContinue(WIND_ATTACK_ANIM);
            if (state == 2) return event.setAndContinue(DEFENCE_STUN_ANIM);
            if (state == 3) return event.setAndContinue(WHEEL_ANIM);
            if (state == 13 || state == 14) return event.setAndContinue(IDLE_ANIM);
            if (state == 4) return event.setAndContinue(ARROW_ATTACK_ANIM);
            if (state == 5 || state == 12) return event.setAndContinue(PRAY_ANIM);
            if (state == 8) return event.setAndContinue(CINEMATIC_MERCY_ANIM);
            if (state == 9) return event.setAndContinue(CINEMATIC_DEATH_ANIM);
            if (state == 10) return event.setAndContinue(CINEMATIC_SPAWN_ANIM);

            return event.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}