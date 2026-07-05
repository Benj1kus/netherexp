package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BelieverVillagerEntity extends PathfinderMob implements GeoEntity, Merchant, Npc {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private Player tradingPlayer;
    @Nullable
    private MerchantOffers offers;
    private int villagerLevel = 1;
    private int villagerXp = 0;
    private int soundCooldown = 0;

    public BelieverVillagerEntity(EntityType<? extends PathfinderMob> type, Level level) {
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
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D) {
            @Override public boolean canUse() { return tradingPlayer == null && super.canUse(); }
        });
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override public boolean canUse() { return tradingPlayer == null && super.canUse(); }
        });
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.soundCooldown > 0) this.soundCooldown--;
    }

    @Override
    public void travel(Vec3 travelVector) {
        super.travel(travelVector);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && this.isAlive() && this.getTradingPlayer() == null) {
            if (!this.level().isClientSide()) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), this.villagerLevel);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    public void openTradingScreen(Player player, Component displayName, int level) {
        java.util.OptionalInt optionalInt = player.openMenu(new SimpleMenuProvider((containerId, playerInv, p) -> {
            return new MerchantMenu(containerId, playerInv, this);
        }, displayName));

        if (optionalInt.isPresent()) {
            MerchantOffers merchantOffers = this.getOffers();
            if (!merchantOffers.isEmpty()) {
                player.sendMerchantOffers(optionalInt.getAsInt(), merchantOffers, level, this.villagerXp, this.showProgressBar(), true);
            }
        }
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.generateLevelTrades(1);
        }
        return this.offers;
    }

    private void generateLevelTrades(int level) {
        if (level == 1) {
            this.offers.add(new MerchantOffer(new ItemStack(Items.COOKED_PORKCHOP, 4), new ItemStack(NetherExp.AZAZEL_GUIDE_BOOK_ITEM.get(), 1), 10, 2, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.COOKED_BEEF, 5), new ItemStack(Items.EMERALD, 1), 10, 2, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.BREAD, 10), new ItemStack(NetherExp.AZAZEL_GUIDE_BOOK_ITEM.get(), 1), 10, 2, 0.05F));
        }
        else if (level == 2) {
            this.offers.add(new MerchantOffer(new ItemStack(Items.LEATHER, 4), new ItemStack(NetherExp.CRIMSON_WEB_ITEM.get(), 2), 10, 10, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.STRING, 2), new ItemStack(NetherExp.CRIMSON_WEB_ITEM.get(), 2), 10, 10, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.HONEY_BLOCK, 2), new ItemStack(NetherExp.CRIMSON_HONEY_BLOCK_ITEM.get(), 1), 10, 10, 0.05F));
        }
        else if (level == 3) {
            this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(NetherExp.ENTRANCE_ITEM.get(), 3), 10, 15, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 4), new ItemStack(NetherExp.ENTRANCE_ITEM.get(), 3), 10, 15, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.GLASS_PANE, 4), new ItemStack(NetherExp.MOSAIC_CHURCH_ITEM.get(), 2), 10, 15, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.GLASS, 2), new ItemStack(NetherExp.MOSAIC_CHURCH_ITEM.get(), 2), 10, 15, 0.05F));
        }
        else if (level == 4) {
            this.offers.add(new MerchantOffer(new ItemStack(Items.COBBLESTONE, 10), new ItemStack(NetherExp.COBBLED_SAMSONIT_ITEM.get(), 20), 10, 20, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.STONE, 5), new ItemStack(NetherExp.COBBLED_SAMSONIT_ITEM.get(), 20), 10, 20, 0.05F));
        }
        else if (level == 5) {
            
            this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(NetherExp.BLACKSTONE_PLANT_ITEM.get(), 1), 5, 30, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 1), new ItemStack(NetherExp.BLACKSTONE_AXON_ITEM.get(), 1), 5, 30, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(NetherExp.BLACKSTONE_PLANT_ITEM.get(), 1), 5, 30, 0.05F));
            this.offers.add(new MerchantOffer(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(NetherExp.BLACKSTONE_AXON_ITEM.get(), 1), 5, 30, 0.05F));
        }
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();

        int xp = offer.getXp();
        if (xp > 0) {
            this.villagerXp += xp;
            checkLevelUp();
        }

        if (!this.level().isClientSide()) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, this.getVoicePitch());
        }
    }

    private void checkLevelUp() {
        int targetXp = getExperienceForLevel(this.villagerLevel + 1);
        if (this.villagerLevel < 5 && this.villagerXp >= targetXp) {
            this.villagerLevel++;
            this.generateLevelTrades(this.villagerLevel);

            if (!this.level().isClientSide()) {
                this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.5F, 1.0F);
            }
        }
    }

    private int getExperienceForLevel(int level) {
        return switch (level) {
            case 2 -> 10;
            case 3 -> 50;
            case 4 -> 100;
            case 5 -> 200;
            default -> 0;
        };
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        if (!this.level().isClientSide() && this.soundCooldown <= 0 && !stack.isEmpty()) {
            this.playSound(this.getNotifyTradeSound(), this.getSoundVolume(), this.getVoicePitch());
            this.soundCooldown = 20;
        }
    }

    @Override public void setTradingPlayer(@Nullable Player player) { this.tradingPlayer = player; }
    @Nullable @Override public Player getTradingPlayer() { return this.tradingPlayer; }
    @Override public void overrideOffers(@Nullable MerchantOffers offers) { this.offers = offers; }
    @Override public int getVillagerXp() { return this.villagerXp; }
    @Override public void overrideXp(int xp) { this.villagerXp = xp; }
    @Override public boolean showProgressBar() { return true; } 
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_TRADE; }
    @Override public boolean isClientSide() { return this.level().isClientSide(); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("VillagerLevel", this.villagerLevel);
        tag.putInt("VillagerXp", this.villagerXp);
        if (this.offers != null) {
            tag.put("Offers", this.offers.createTag());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("VillagerLevel")) this.villagerLevel = tag.getInt("VillagerLevel");
        if (tag.contains("VillagerXp")) this.villagerXp = tag.getInt("VillagerXp");
        if (tag.contains("Offers", 10)) {
            this.offers = new MerchantOffers(tag.getCompound("Offers"));
        }
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            if (cause.getEntity() instanceof Player killer) {
                killer.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BAD_OMEN, 36000, 0));
            }
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 30, 0.3D, 0.5D, 0.3D, 0.05D);
            if (NetherExp.STATUE_BOSSUNIT.isPresent()) {
                Entity statue = NetherExp.STATUE_BOSSUNIT.get().create(serverLevel);
                if (statue instanceof StatueBossunitEntity statueBossunitEntity) {
                    statueBossunitEntity.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                    serverLevel.addFreshEntity(statueBossunitEntity);
                }
            }
        }
        super.die(cause);
    }

    private static final RawAnimation RUN_ANIM = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, event -> {
            if (event.isMoving()) {
                if (this.getDeltaMovement().horizontalDistanceSqr() > 0.015) {
                    return event.setAndContinue(RUN_ANIM);
                }
                return event.setAndContinue(WALK_ANIM);
            }
            return event.setAndContinue(IDLE_ANIM);
        }));
    }

    @Nullable @Override protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT; }
    @Nullable @Override protected SoundEvent getHurtSound(DamageSource damageSourceIn) { return SoundEvents.VILLAGER_HURT; }
    @Nullable @Override protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}