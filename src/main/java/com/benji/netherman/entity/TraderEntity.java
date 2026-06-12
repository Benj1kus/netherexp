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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
    public static final EntityDataAccessor<Boolean> SHOW_HINT = SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> TRADE_STATE = SynchedEntityData.defineId(TraderEntity.class, EntityDataSerializers.INT);

    public static final ResourceLocation GOLD_TRADE_LOOT = new ResourceLocation(NetherExp.MODID, "gameplay/trader_gold");
    public static final ResourceLocation DIAMOND_TRADE_LOOT = new ResourceLocation(NetherExp.MODID, "gameplay/trader_diamond");
    public static final ResourceLocation NETHERITE_TRADE_LOOT = new ResourceLocation(NetherExp.MODID, "gameplay/trader_netherite");

    public int hintTicks = 0;
    public int tradeTimer = 0;
    private int pendingRewardTier = 0;
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
            return InteractionResult.PASS;
        }

        if (isAcceptedItem(stack)) {
            if (stack.is(Items.GOLD_INGOT)) pendingRewardTier = 1;
            else if (stack.is(Items.DIAMOND)) pendingRewardTier = 2;
            else if (stack.is(Items.NETHERITE_INGOT)) pendingRewardTier = 3;

            if (!player.isCreative()) stack.shrink(1);

            this.tradingPlayer = player;
            this.entityData.set(TRADE_STATE, 1);
            this.tradeTimer = 80;
            this.getLookControl().setLookAt(player);
            this.playSound(SoundEvents.WANDERING_TRADER_TRADE, 1.0F, this.getVoicePitch());

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0F, this.getVoicePitch());
            this.hintTicks = 60;
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
            if (tradeState > 0) {
                this.tradeTimer--;
                if (tradeState == 1 && this.tradeTimer <= 0) {
                    this.giveRewardToPlayer();

                    this.playSound(SoundEvents.ITEM_PICKUP, 1.0F, 1.0F); // Звук BOP
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 10, 0.3, 0.3, 0.3, 0.0);
                    }

                    this.entityData.set(TRADE_STATE, 2);
                    this.tradeTimer = 80;
                }
                else if (tradeState == 2 && this.tradeTimer <= 0) {
                    this.entityData.set(TRADE_STATE, 0);
                    this.tradingPlayer = null;
                }
            } else {
                if (this.hintTicks > 0) {
                    this.hintTicks--;
                    this.entityData.set(SHOW_HINT, true);
                } else if (this.tickCount % 10 == 0) {
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

    private void giveRewardToPlayer() {
        if (this.tradingPlayer == null || !(this.level() instanceof ServerLevel serverLevel)) return;
        ResourceLocation lootTableId = null;
        if (this.pendingRewardTier == 1) lootTableId = GOLD_TRADE_LOOT;
        else if (this.pendingRewardTier == 2) lootTableId = DIAMOND_TRADE_LOOT;
        else if (this.pendingRewardTier == 3) lootTableId = NETHERITE_TRADE_LOOT;

        if (lootTableId != null) {
            LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableId);
            LootParams lootParams = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .create(LootContextParamSets.GIFT);
            List<ItemStack> generatedLoot = lootTable.getRandomItems(lootParams);
            for (ItemStack reward : generatedLoot) {
                Vec3 dir = this.tradingPlayer.position().subtract(this.position()).normalize().scale(0.3);
                ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 1.0D, this.getZ(), reward);
                itemEntity.setDeltaMovement(dir.x, 0.3D, dir.z);
                this.level().addFreshEntity(itemEntity);
            }
        }
    }
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