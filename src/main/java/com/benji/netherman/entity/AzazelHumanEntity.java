package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.config.AzazelConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class AzazelHumanEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Integer> BOSS_STATE = SynchedEntityData.defineId(AzazelHumanEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DIALOGUE_TICK = SynchedEntityData.defineId(AzazelHumanEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> ATTACK_TIMER = SynchedEntityData.defineId(AzazelHumanEntity.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<Boolean> IS_PHASE_2 = SynchedEntityData.defineId(AzazelHumanEntity.class, EntityDataSerializers.BOOLEAN);

    public float getDamageTakenRecently() { return this.damageTakenRecently; }
    public void resetDamageTaken() { this.damageTakenRecently = 0.0F; }
    public int getDefendCooldown() { return this.defendCooldown; }
    public void setDefendCooldown(int ticks) { this.defendCooldown = ticks; }
    public int forcedAttackGoal = 0;
    private int crackStage = 0;
    public boolean phase2Triggered = false;

    private final ServerBossEvent bossEvent = (ServerBossEvent) (new ServerBossEvent(Component.literal("Azazel, The Awakened"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
    private float damageTakenRecently = 0.0F;
    private int damageTimer = 0;
    private int defendCooldown = 0; 
    private static final int[] LINE_LENGTHS = {31, 33, 40, 31, 28, 33, 35, 39, 29, 29};

    public AzazelHumanEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 100.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BOSS_STATE, 0);
        this.entityData.define(DIALOGUE_TICK, 0);
        this.entityData.define(ATTACK_TIMER, 0);
        this.entityData.define(IS_PHASE_2, false);
    }


    
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
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.goalSelector.addGoal(0, new AzazelHumanUltimateGoal(this));
        this.goalSelector.addGoal(0, new AzazelHumanPhase2Goal(this));
        this.goalSelector.addGoal(1, new AzazelHumanMeleeGoal(this));
        this.goalSelector.addGoal(2, new AzazelHumanLongRangeGoal(this));
        this.goalSelector.addGoal(3, new AzazelHumanMidRangeGoal(this));
        this.goalSelector.addGoal(4, new AzazelHumanMovementGoal(this));
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
            if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                ServerLevel currentLevel = (ServerLevel) this.level();
                ServerLevel respawnLevel = currentLevel.getServer().getLevel(serverPlayer.getRespawnDimension());
                if (respawnLevel == null) respawnLevel = currentLevel.getServer().overworld();

                BlockPos respawnPos = serverPlayer.getRespawnPosition();
                float respawnAngle = serverPlayer.getRespawnAngle();

                if (respawnPos != null) {
                    Optional<Vec3> safePos = Player.findRespawnPositionAndUseSpawnBlock(respawnLevel, respawnPos, respawnAngle, serverPlayer.isRespawnForced(), true);
                    if (safePos.isPresent()) {
                        serverPlayer.teleportTo(respawnLevel, safePos.get().x, safePos.get().y, safePos.get().z, respawnAngle, 0.0F);
                    } else {
                        BlockPos sharedSpawn = respawnLevel.getSharedSpawnPos();
                        serverPlayer.teleportTo(respawnLevel, sharedSpawn.getX(), sharedSpawn.getY(), sharedSpawn.getZ(), respawnAngle, 0.0F);
                    }
                } else {
                    BlockPos sharedSpawn = respawnLevel.getSharedSpawnPos();
                    serverPlayer.teleportTo(respawnLevel, sharedSpawn.getX(), sharedSpawn.getY(), sharedSpawn.getZ(), respawnAngle, 0.0F);
                }

                respawnLevel.playSound(null, serverPlayer.blockPosition(), ModSounds.BELL_BEAST_LAUGH.get(), SoundSource.PLAYERS, 1.5F, 1.0F);

                DustParticleOptions redMagic = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
                respawnLevel.sendParticles(serverPlayer, redMagic, true, serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(), 80, 0.8D, 1.0D, 0.8D, 0.1D);

                BlockPos barrelPos = serverPlayer.blockPosition();
                for (int[] offset : new int[][]{{1,0,0}, {-1,0,0}, {0,0,1}, {0,0,-1}, {0,1,0}}) {
                    BlockPos checkPos = serverPlayer.blockPosition().offset(offset[0], offset[1], offset[2]);
                    if (respawnLevel.getBlockState(checkPos).canBeReplaced()) {
                        barrelPos = checkPos;
                        break;
                    }
                }
                respawnLevel.setBlockAndUpdate(barrelPos, Blocks.BARREL.defaultBlockState());

                respawnLevel.playSound(null, barrelPos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0F, 1.5F);

                respawnLevel.sendParticles(serverPlayer, ParticleTypes.TOTEM_OF_UNDYING, true, barrelPos.getX() + 0.5D, barrelPos.getY() + 1.0D, barrelPos.getZ() + 0.5D, 60, 0.4D, 0.5D, 0.4D, 0.2D);

                net.minecraft.world.level.block.entity.BlockEntity blockEntity = respawnLevel.getBlockEntity(barrelPos);
                if (blockEntity instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity barrel) {
                    barrel.setItem(0, new ItemStack(NetherExp.AZAZEL_HELMET.get(), 1));
                    barrel.setItem(1, new ItemStack(NetherExp.AZAZEL_CHESTPLATE.get(), 1));
                    barrel.setItem(2, new ItemStack(NetherExp.AZAZEL_LEGGINGS.get(), 1));
                    barrel.setItem(3, new ItemStack(NetherExp.AZAZEL_BOOTS.get(), 1));
                    barrel.setItem(4, new ItemStack(NetherExp.QUOTA.get(), 1));
                    barrel.setItem(5, new ItemStack(com.benji.netherman.NetherExp.FAITH_PART.get(), 5));
                    barrel.setItem(6, new ItemStack(com.benji.netherman.NetherExp.FAITH_ESSENCE.get(), 1));
                }

                this.bossEvent.removeAllPlayers();
                serverPlayer.getPersistentData().putBoolean("AzazelCultist", true);
                com.benji.netherman.QuotaManager.generateNewQuota(serverPlayer);
                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;
        int state = this.entityData.get(BOSS_STATE);


        if (state == 100 || state == 101 || state == 110 || state == 111) {
            return false;
        }

        if (this.getHealth() - amount <= 0.0F && state < 100) {
            startDeathCinematic();
            return false;
        }

        if (state < 5) {
            if (state == 3 && source.getEntity() instanceof Player) {
                this.entityData.set(BOSS_STATE, 4);
                this.entityData.set(DIALOGUE_TICK, 0);
                this.playSound(ModSounds.LAUGH.get(), 2.0F, 1.0F);

                Vec3 forward = Vec3.directionFromRotation(0, this.getYRot()).normalize();
                this.setPos(this.getX() + forward.x * 2.0D, this.getY(), this.getZ() + forward.z * 2.0D);

                for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D))) {
                    p.addEffect(new MobEffectInstance(NetherExp.PRAEMIUM.get(), -1, 0, false, false, true));
                }
            }
            return false;
        }

        
        if (state == 11 || state == 12 || state == 13 || state == 22 || state == 70) {
            if (source.getEntity() != null) {
                this.playSound(ModSounds.DODGE.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);

                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 3.5D, this.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
                }
            }
            return false;
        }

        
        if (state >= 5 && this.random.nextFloat() < 0.25F) {
            this.entityData.set(BOSS_STATE, 40); 
            this.entityData.set(ATTACK_TIMER, 40); 
            this.playSound(ModSounds.DODGE.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);

            if (this.level() instanceof ServerLevel sl) {
                
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.5D, this.getZ(), 5, 0.5, 0.5, 0.5, 0.05);
            }
            return false; 
        }

        
        if (state >= 5) {
            this.damageTakenRecently += amount;
            this.damageTimer = 40; 
        }

        return super.hurt(source, amount);
    }

    private void startDeathCinematic() {
        this.entityData.set(BOSS_STATE, 100);
        this.entityData.set(DIALOGUE_TICK, 0);
        this.getNavigation().stop();
        this.setTarget(null);
        this.setHealth(1.0F);

        // remove boss-theme
        java.util.List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(100.0D));
        for (Player p : nearbyPlayers) {
            p.removeEffect(NetherExp.PRAEMIUM.get());
        }
    }

    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        int state = this.entityData.get(BOSS_STATE);
        if (state >= 100) {
            return false;
        }
        return super.canAttack(target);
    }


    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            int state = this.entityData.get(BOSS_STATE);
            int tick = this.entityData.get(DIALOGUE_TICK);

            if (state == 100 || state == 101) {
                tick++;
                this.entityData.set(DIALOGUE_TICK, tick);

                if (tick == 1) {
                    this.playSound(ModSounds.ROAR.get(), 4.0F, 1.0F);
                }

                if (tick > 45 && tick <= 93 && tick % 2 == 0) {
                    this.playSound(ModSounds.AZAZEL_VOICE.get(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                }

                if (tick == 120) {
                    this.entityData.set(BOSS_STATE, 101);
                    this.playSound(ModSounds.LAUGH.get(), 2.0F, 1.0F);
                }

                if (tick >= 180) {
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 4.0F, 1.0F);
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 1, 0, 0, 0, 0);
                        serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 1.0D, 1.0D, 1.0D, 0.1D);

                        for (int i = 0; i < 30; i++) {
                            net.minecraft.world.item.Item goldItem = this.random.nextBoolean() ? Items.DIAMOND : net.minecraft.world.item.Items.GOLD_NUGGET;
                            net.minecraft.world.entity.item.ItemEntity gold = new net.minecraft.world.entity.item.ItemEntity(
                                    serverLevel, this.getX(), this.getY() + 1.0D, this.getZ(), new net.minecraft.world.item.ItemStack(goldItem, 1)
                            );
                            gold.setDeltaMovement((this.random.nextDouble() - 0.5D) * 0.5D, 0.5D + this.random.nextDouble() * 0.3D, (this.random.nextDouble() - 0.5D) * 0.5D);
                            serverLevel.addFreshEntity(gold);
                        }

                        BlockPos barrelPos = this.blockPosition();
                        serverLevel.setBlockAndUpdate(barrelPos, Blocks.BARREL.defaultBlockState());

                        net.minecraft.world.level.block.entity.BlockEntity blockEntity = serverLevel.getBlockEntity(barrelPos);
                        if (blockEntity instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity barrel) {
                            java.util.List<Integer> availableSlots = new java.util.ArrayList<>();
                            for (int i = 0; i < 27; i++) availableSlots.add(i);
                            java.util.Collections.shuffle(availableSlots);

                            net.minecraft.world.item.ItemStack[] loot = new net.minecraft.world.item.ItemStack[] {
                                    new net.minecraft.world.item.ItemStack(NetherExp.AZAZEL_SPEAR.get(), 1),
                                    new net.minecraft.world.item.ItemStack(NetherExp.MUSIC_DISC_QUAR.get(), 1),
                                    new net.minecraft.world.item.ItemStack(NetherExp.MUSIC_DISC_SACRED.get(), 1),
                                    new net.minecraft.world.item.ItemStack(NetherExp.MUSIC_DISC_MAZE.get(), 1),
                                    new net.minecraft.world.item.ItemStack(NetherExp.CHANCE_TOTEM.get(), 2),
                                    new net.minecraft.world.item.ItemStack(NetherExp.AZAZEL_SHIELD.get(), 2),
                                    new net.minecraft.world.item.ItemStack(NetherExp.AZAZEL_CHESTPLATE.get(), 1),
                                    new net.minecraft.world.item.ItemStack(NetherExp.AZAZEL_HELMET.get(), 1),
                                    new net.minecraft.world.item.ItemStack(NetherExp.AZAZEL_LEGGINGS.get(), 1),
                                    new net.minecraft.world.item.ItemStack(NetherExp.AZAZEL_BOOTS.get(), 1),
                                    new net.minecraft.world.item.ItemStack(NetherExp.AZAZEL_TROPHY_ITEM.get(), 1),
                                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 55),
                                    new net.minecraft.world.item.ItemStack(Items.MILK_BUCKET, 1),
                                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_SCRAP, 20),
                                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE, 10),
                            };

                            for (int i = 0; i < loot.length && i < availableSlots.size(); i++) {
                                barrel.setItem(availableSlots.get(i), loot[i]);
                            }
                        }
                    }
                    this.bossEvent.removeAllPlayers();
                    this.discard();
                }
                return;
            }

            if (state == 110 || state == 111) {
                this.setTarget(null);
                tick++;
                this.entityData.set(DIALOGUE_TICK, tick);

                if (state == 110) {
                    if (tick == 1) {
                        this.playSound(SoundEvents.WARDEN_EMERGE, 1.5F, 1.0F);
                    }

                    if (this.level() instanceof ServerLevel sl) {
                        BlockState dirt = Blocks.PODZOL.defaultBlockState();
                        for (int i = 0; i < 4; i++) {
                            double dx = (this.random.nextDouble() - 0.5) * 4.0;
                            double dz = (this.random.nextDouble() - 0.5) * 4.0;
                            sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, dirt), this.getX() + dx, this.getY(), this.getZ() + dz, 1, 0.0, 0.3, 0.0, 0.1);
                            sl.sendParticles(ParticleTypes.LAVA, this.getX() + dx, this.getY(), this.getZ() + dz, 1, 0.0, 0.5, 0.0, 0.1);
                        }
                    }

                    if (tick >= 70) {
                        this.entityData.set(BOSS_STATE, 111);
                        this.entityData.set(DIALOGUE_TICK, 0);
                    }
                }
                else if (state == 111) {
                    this.setYRot(180.0F);
                    this.setYBodyRot(180.0F);
                    this.setYHeadRot(180.0F);

                    this.setDeltaMovement(0, this.getDeltaMovement().y, -0.2D);

                    if (tick % 26 == 0) {
                        this.playSound(ModSounds.GUARDIAN_WALK.get(), 4.0F, 0.6F + this.random.nextFloat() * 0.4F);
                    }

                    if (this.level() instanceof ServerLevel sl) {
                        java.util.List<Player> nearbyPlayers = sl.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(20.0D));
                        for (Player p : nearbyPlayers) {
                            com.benji.netherman.client.ClientFogHandler.isInsideMansion = true;
                        }

                        if (tick % 5 == 0) {
                            BlockPos center = this.blockPosition();
                            boolean brokeBlocks = false;

                            for (int x = -3; x <= 3; x++) {
                                for (int y = 0; y <= 14; y++) {
                                    BlockPos target = center.offset(x, y, -2);
                                    BlockState block = sl.getBlockState(target);


                                    boolean isEdge = (Math.abs(x) == 3 || y >= 12);
                                    if (isEdge && this.random.nextFloat() > 0.7F) continue;

                                    if (!block.isAir() && block.getDestroySpeed(sl, target) >= 0) {
                                        sl.destroyBlock(target, false);
                                        brokeBlocks = true;
                                    }
                                }
                            }
                            if (brokeBlocks) {
                                this.playSound(ModSounds.DODGE.get(), 1.0F, 0.8F);
                            }
                        }

                        if (tick % 40 == 0 && this.random.nextFloat() < 0.6F) {
                            var quake = NetherExp.EARTHQUAKE_ENTITY.get().create(sl);
                            if (quake != null) {
                                double dx = (this.random.nextDouble() - 0.5) * 20.0D;
                                double dz = (this.random.nextDouble() - 0.5) * 20.0D;
                                BlockPos quakePos = BlockPos.containing(this.getX() + dx, this.getY(), this.getZ() + dz);
                                while (sl.isEmptyBlock(quakePos.below()) && quakePos.getY() > sl.getMinBuildHeight()) {
                                    quakePos = quakePos.below();
                                }
                                quake.moveTo(quakePos.getX() + 0.5D, quakePos.getY(), quakePos.getZ() + 0.5D, 0, 0);
                                sl.addFreshEntity(quake);
                            }
                        }
                    }
                }
                return;
            }

            if (state >= 5 && this.isAlive() && this.forcedAttackGoal == 0) {
                float hpPct = this.getHealth() / this.getMaxHealth();
                if (hpPct <= 0.3F && !this.phase2Triggered) {
                    this.phase2Triggered = true;
                    this.forcedAttackGoal = 5;
                } else if (hpPct <= 0.7F && this.crackStage == 0) {
                    this.crackStage = 1;
                    this.forcedAttackGoal = 4;
                } else if (hpPct <= 0.5F && this.crackStage == 1) {
                    this.crackStage = 2;
                    this.forcedAttackGoal = 4;
                } else if (hpPct <= 0.1F && this.crackStage == 2) {
                    this.crackStage = 3;
                    this.forcedAttackGoal = 4;
                }
            }

            if (this.defendCooldown > 0) this.defendCooldown--;

            if (this.damageTimer > 0) {
                this.damageTimer--;
            } else {
                this.damageTakenRecently = 0.0F; 
            }

            if ((state == 10 || state == 11) && this.tickCount % 35 == 0) {
                this.playSound(ModSounds.GUARDIAN_WALK.get(), 4.0F, 0.6F + this.random.nextFloat() * 0.4F);
            }
            
            else if (state == 12 && this.tickCount % 12 == 0) {
                
                this.playSound(ModSounds.GUARDIAN_WALK.get(), 4.0F, 1.2F + this.random.nextFloat() * 0.2F);
            }

            if (state == 14 || state == 40) {
                int attackTimer = this.entityData.get(ATTACK_TIMER);
                if (attackTimer > 0) {
                    this.entityData.set(ATTACK_TIMER, attackTimer - 1);

                    if (state == 40 && attackTimer == 20) {
                        this.playSound(ModSounds.LAUGH.get(), 2.0F, 1.0F);
                    }
                } else {
                    this.entityData.set(BOSS_STATE, 5);
                }
            }

            if (state >= 1 && state <= 4) {
                tick++;
                this.entityData.set(DIALOGUE_TICK, tick);

                if (state == 1 && tick >= 20) {
                    this.entityData.set(BOSS_STATE, 2);
                    this.entityData.set(DIALOGUE_TICK, 0);
                } else if (state == 2) {
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
                        this.entityData.set(DIALOGUE_TICK, 0);
                    }
                } else if (state == 3) {
                    if (tick >= 200) {
                        this.entityData.set(BOSS_STATE, 4);
                        this.entityData.set(DIALOGUE_TICK, 0);
                        this.playSound(ModSounds.LAUGH.get(), 2.0F, 1.0F);

                        Vec3 forward = Vec3.directionFromRotation(0, this.getYRot()).normalize();
                        this.setPos(this.getX() + forward.x * 2.0D, this.getY(), this.getZ() + forward.z * 2.0D);

                        for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D))) {
                            p.addEffect(new MobEffectInstance(NetherExp.PRAEMIUM.get(), -1, 0, false, false, true));
                        }
                    }
                } else if (state == 4 && tick >= 30) {
                    this.entityData.set(BOSS_STATE, 5);
                    for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D))) {
                        this.bossEvent.addPlayer(p);
                    }
                }
            }

            if (state == 5 && this.tickCount % 20 == 0) {
                for (ServerPlayer p : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(100.0D))) {
                    p.addEffect(new MobEffectInstance(NetherExp.PRAEMIUM.get(), -1, 0, false, false, true));
                }


                if (this.getTarget() != null && this.forcedAttackGoal == 0) {
                    float attackChance = this.entityData.get(IS_PHASE_2) ? 0.15F : 0.05F;
                    if (this.random.nextFloat() < attackChance) {
                        this.forcedAttackGoal = this.random.nextInt(3) + 1;
                    }
                }
            }
        }
    }

    
    public void triggerScreenShake(float intensity, int ticks) {
        if (this.level() instanceof ServerLevel sl) {
            for (ServerPlayer player : sl.getPlayers(p -> p.distanceToSqr(this) < 4000.0D)) {
                
                
            }
        }
        this.level().broadcastEntityEvent(this, (byte) 101); 
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 101) { 
            
            com.benji.netherman.client.ClientZoneAmbientEvents.startScreenShake(30, 2.0F);

            
            BlockState state = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            for (int i = 0; i < 40; i++) {
                double dx = (this.random.nextDouble() - 0.5) * 6.0;
                double dz = (this.random.nextDouble() - 0.5) * 6.0;
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), this.getX() + dx, this.getY() + 0.5, this.getZ() + dz, 0.0, 0.5, 0.0);
                this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX() + dx, this.getY() + 1.0, this.getZ() + dz, 0.0, 0.2, 0.0);
            }
        } else {
            super.handleEntityEvent(id);
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

                case 10 -> event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
                case 11 -> event.setAndContinue(RawAnimation.begin().thenLoop("defend_walk"));
                case 12 -> event.setAndContinue(RawAnimation.begin().thenLoop("defend_run"));
                case 13 -> event.setAndContinue(RawAnimation.begin().thenPlay("jump_attack"));

                case 30 -> event.setAndContinue(RawAnimation.begin().thenPlay("spear_long").thenLoop("idle"));
                case 31 -> event.setAndContinue(RawAnimation.begin().thenPlay("spear_mid").thenLoop("idle"));
                case 32 -> event.setAndContinue(RawAnimation.begin().thenPlay("smoke").thenLoop("idle"));
                case 40 -> event.setAndContinue(RawAnimation.begin().thenPlay("dodge").thenLoop("idle"));
                case 50 -> event.setAndContinue(RawAnimation.begin().thenPlay("scythe_attack").thenLoop("idle"));
                case 51 -> event.setAndContinue(RawAnimation.begin().thenPlay("spear_attack").thenLoop("idle"));
                case 52 -> event.setAndContinue(RawAnimation.begin().thenPlay("spear_attack").thenLoop("idle"));
                case 53 -> event.setAndContinue(RawAnimation.begin().thenPlay("spear_attack").thenLoop("idle"));
                case 60 -> event.setAndContinue(RawAnimation.begin().thenPlay("leg_attack").thenLoop("idle"));
                case 70 -> event.setAndContinue(RawAnimation.begin().thenPlay("phase").thenLoop("idle"));

                case 100 -> event.setAndContinue(RawAnimation.begin().thenPlay("fall").thenLoop("fall_idle"));
                case 101 -> event.setAndContinue(RawAnimation.begin().thenPlay("fall_death"));

                case 110 -> event.setAndContinue(RawAnimation.begin().thenPlay("ground").thenLoop("ground_idle"));
                case 111 -> {
                    if (this.getDeltaMovement().lengthSqr() < 0.01D) {
                        yield event.setAndContinue(RawAnimation.begin().thenLoop("ground_idle"));
                    } else {
                        yield event.setAndContinue(RawAnimation.begin().thenLoop("ground_walk"));
                    }
                }

                case 20 -> event.setAndContinue(RawAnimation.begin().thenPlay("leg_attack").thenLoop("idle"));
                case 21 -> event.setAndContinue(RawAnimation.begin().thenPlay("scythe_attack").thenLoop("idle"));
                case 22 -> event.setAndContinue(RawAnimation.begin().thenPlay("defend_stay").thenLoop("idle"));

                case 14 -> event.setAndContinue(RawAnimation.begin().thenPlay("fall").thenPlay("standup"));

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
        tag.putInt("CrackStage", this.crackStage);
        tag.putBoolean("IsPhase2", this.entityData.get(IS_PHASE_2));
        tag.putBoolean("Phase2Triggered", this.phase2Triggered);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400 + this.random.nextInt(600);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return this.random.nextBoolean() ? ModSounds.HUMAN_DAMAGE_1.get() : ModSounds.HUMAN_DAMAGE_2.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
            int rand = this.random.nextInt(3);
            return rand == 0 ? ModSounds.SPEECH_1.get() : (rand == 1 ? ModSounds.SPEECH_2.get() : ModSounds.SPEECH_3.get());
        }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(BOSS_STATE, tag.getInt("BossState"));
        this.entityData.set(DIALOGUE_TICK, tag.getInt("DialogueTick"));
        this.entityData.set(IS_PHASE_2, tag.getBoolean("IsPhase2"));
        this.phase2Triggered = tag.getBoolean("Phase2Triggered");
        this.crackStage = tag.getInt("CrackStage");

        if (!this.level().isClientSide() && this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AzazelConfig.HUMAN_MAX_HEALTH.get());
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(AzazelConfig.HUMAN_MOVEMENT_SPEED.get());
            this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(AzazelConfig.HUMAN_KNOCKBACK_RESISTANCE.get());
        }
    }
}