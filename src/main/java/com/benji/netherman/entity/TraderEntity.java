package com.benji.netherman.entity;

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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class TraderEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Синхронизация состояний для анимаций и рендера подсказок
    public static final EntityDataAccessor<Boolean> SHOW_HINT = SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.BOOLEAN);
    // 0 = Не торгует, 1 = Открывает рюкзак (trade_open), 2 = Закрывает рюкзак (trade_close)
    public static final EntityDataAccessor<Integer> TRADE_STATE = SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.INT);

    public int hintTicks = 0;
    public int tradeTimer = 0;
    private int pendingRewardTier = 0; // 1 = Золото, 2 = Алмаз, 3 = Незерит
    private Player tradingPlayer = null;

    public TraderEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOW_HINT, false);
        this.entityData.define(TRADE_STATE, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    private boolean isAcceptedItem(ItemStack stack) {
        return stack.is(Items.GOLD_INGOT) || stack.is(Items.DIAMOND) || stack.is(Items.NETHERITE_INGOT);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.entityData.get(TRADE_STATE) != 0) {
            return InteractionResult.PASS; // Игнорируем клики, пока идет торговля
        }

        if (isAcceptedItem(stack)) {
            // ОПРЕДЕЛЯЕМ ТИР ТОРГОВЛИ
            if (stack.is(Items.GOLD_INGOT)) pendingRewardTier = 1;
            else if (stack.is(Items.DIAMOND)) pendingRewardTier = 2;
            else if (stack.is(Items.NETHERITE_INGOT)) pendingRewardTier = 3;

            if (!player.isCreative()) stack.shrink(1);

            this.tradingPlayer = player;
            this.entityData.set(TRADE_STATE, 1);
            this.tradeTimer = 80; // 4 секунды (trade_open)

            // Торговец смотрит на игрока и мычит
            this.getLookControl().setLookAt(player);
            this.playSound(SoundEvents.WANDERING_TRADER_TRADE, 1.0F, this.getVoicePitch());

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            // ИГРОК КЛИКНУЛ НЕПРАВИЛЬНЫМ ПРЕДМЕТОМ
            this.playSound(SoundEvents.VILLAGER_NO, 1.0F, this.getVoicePitch());
            this.hintTicks = 60; // Показываем trader_hint.png на 3 сек
            this.entityData.set(SHOW_HINT, true);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5D, this.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            int tradeState = this.entityData.get(TRADE_STATE);

            // ЛОГИКА ТОРГОВЛИ (ТАЙМЕРЫ)
            if (tradeState > 0) {
                this.tradeTimer--;

                // ЗАВЕРШЕНИЕ trade_open
                if (tradeState == 1 && this.tradeTimer <= 0) {
                    this.giveRewardToPlayer();

                    this.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F); // Звук BOP
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 10, 0.3, 0.3, 0.3, 0.0);
                    }

                    this.entityData.set(TRADE_STATE, 2);
                    this.tradeTimer = 80; // 4 секунды (trade_close)
                }
                // ЗАВЕРШЕНИЕ trade_close
                else if (tradeState == 2 && this.tradeTimer <= 0) {
                    this.entityData.set(TRADE_STATE, 0);
                    this.tradingPlayer = null;
                }
            } else {
// ПРОВЕРКА ИГРОКОВ С ВАЛЮТОЙ ВОКРУГ (Оптимизировано под серверный TPS)
                if (this.hintTicks > 0) {
                    this.hintTicks--;
                    this.entityData.set(SHOW_HINT, true); // Принудительно показываем подсказку
                } else if (this.tickCount % 10 == 0) {
                    // Сканируем область только раз в 10 тиков!
                    boolean holdingCurrency = false;
                    List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6.0D));
                    for (Player p : nearbyPlayers) {
                        if (isAcceptedItem(p.getMainHandItem()) || isAcceptedItem(p.getOffhandItem())) {
                            holdingCurrency = true;
                            this.getLookControl().setLookAt(p);
                            break;
                        }
                    }
                    this.entityData.set(SHOW_HINT, holdingCurrency);
                }
            }
        }
    }

    // --- ОПТИМИЗАЦИЯ 1: Кэшируем массив алмазных наград один раз ---
    private static final ItemStack[] DIAMOND_GEAR = {
            new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.DIAMOND_PICKAXE),
            new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.DIAMOND_HELMET),
            new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(Items.DIAMOND_LEGGINGS),
            new ItemStack(Items.DIAMOND_BOOTS)
    };

    private void giveRewardToPlayer() {
        ItemStack reward = ItemStack.EMPTY;

        if (this.pendingRewardTier == 1) { // ЗОЛОТО
            int rand = this.random.nextInt(5);
            reward = switch (rand) {
                case 0 -> new ItemStack(Items.OBSIDIAN, 3);
                case 1 -> new ItemStack(Items.GOLDEN_PICKAXE, 1);
                case 2 -> new ItemStack(Items.GOLD_NUGGET, 8);
                case 3 -> new ItemStack(Items.GOLDEN_CARROT, 10);
                default -> new ItemStack(Items.BLAZE_POWDER, 3);
            };
        } else if (this.pendingRewardTier == 2) { // АЛМАЗ
            int rand = this.random.nextInt(6);
            reward = switch (rand) {
                case 0 -> new ItemStack(Items.BLAZE_ROD, 5);
                case 1 -> new ItemStack(Items.ENDER_PEARL, 2);
                case 2 -> new ItemStack(Items.ENDER_EYE, 1);
                case 3 -> new ItemStack(Items.REDSTONE, 32);
                case 4 -> new ItemStack(Items.PIGLIN_HEAD, 1);
                default -> new ItemStack(Items.GOLDEN_APPLE, 1);
            };
        } else if (this.pendingRewardTier == 3) { // НЕЗЕРИТ
            int rand = this.random.nextInt(5);
            reward = switch (rand) {
                case 0 -> new ItemStack(Items.CRYING_OBSIDIAN, 5);
                case 1 -> new ItemStack(Items.ENDER_PEARL, 5);
                case 2 -> new ItemStack(Items.GOLD_BLOCK, 3);
                case 3 -> new ItemStack(Items.NETHER_STAR, 1);
                default -> {
                    // Берем предмет из статического массива и ОБЯЗАТЕЛЬНО делаем .copy()
                    ItemStack gear = DIAMOND_GEAR[this.random.nextInt(DIAMOND_GEAR.length)].copy();
                    yield EnchantmentHelper.enchantItem(this.random, gear, 30, false);
                }
            };
        }

        if (!reward.isEmpty() && this.tradingPlayer != null) {
            Vec3 dir = this.tradingPlayer.position().subtract(this.position()).normalize().scale(0.3);
            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 1.0D, this.getZ(), reward);
            itemEntity.setDeltaMovement(dir.x, 0.3D, dir.z);
            this.level().addFreshEntity(itemEntity);
        }
    }

    // --- ЛОГИКА ОБЕЗДВИЖИВАНИЯ ---
    @Override
    public void travel(Vec3 travelVector) {
        if (this.entityData.get(TRADE_STATE) > 0) {
            this.getNavigation().stop();
            super.travel(Vec3.ZERO);
        } else {
            super.travel(travelVector);
        }
    }
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    // --- ЗВУКИ ---
    @Override
    public float getVoicePitch() { return 0.8F + this.random.nextFloat() * 0.4F; }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.WANDERING_TRADER_AMBIENT; }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) { return SoundEvents.WANDERING_TRADER_HURT; }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.WANDERING_TRADER_DEATH; }

    private static final RawAnimation TRADE_OPEN_ANIM = RawAnimation.begin().thenPlay("trade_open");
    private static final RawAnimation TRADE_CLOSE_ANIM = RawAnimation.begin().thenPlay("trade_close");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    // --- АНИМАЦИИ ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            int state = this.entityData.get(TRADE_STATE);
            if (state == 1) return event.setAndContinue(TRADE_OPEN_ANIM);
            if (state == 2) return event.setAndContinue(TRADE_CLOSE_ANIM);
            if (event.isMoving()) return event.setAndContinue(WALK_ANIM);
            return event.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TradeState", this.entityData.get(TRADE_STATE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(TRADE_STATE, tag.getInt("TradeState"));
    }
}