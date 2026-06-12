package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BlacksmithEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = Скрыта, 1 = blacksmith_hint
    public static final EntityDataAccessor<Boolean> SHOW_HINT = SynchedEntityData.defineId(BlacksmithEntity.class, EntityDataSerializers.BOOLEAN);

    // 0 = Ждет оплату, 1 = Получил Золото (разборка), 2 = Получил Незерит (починка)
    public static final EntityDataAccessor<Integer> PAYMENT_STATE = SynchedEntityData.defineId(BlacksmithEntity.class, EntityDataSerializers.INT);

    public int hintTimer = 0;
    private BlockPos targetAnvil = null;

    public BlacksmithEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // Как у жителя
                .add(Attributes.MOVEMENT_SPEED, 0.0D) // Не двигается
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D); // Не откидывается
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOW_HINT, false);
        this.entityData.define(PAYMENT_STATE, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    // Защита от сдвигов
    @Override
    public void knockback(double strength, double x, double z) {}
    @Override
    public void push(double x, double y, double z) {}

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) return InteractionResult.sidedSuccess(true);

        ItemStack stack = player.getItemInHand(hand);
        int state = this.entityData.get(PAYMENT_STATE);

        // --- ЛОГИКА 1: ПРИЕМ ОПЛАТЫ ---
        if (state == 0) {
            if (stack.is(Items.RAW_GOLD)) {
                if (!player.isCreative()) stack.shrink(1);
                this.entityData.set(PAYMENT_STATE, 1);
                this.playSound(SoundEvents.VILLAGER_YES, 1.0F, this.getVoicePitch());
                return InteractionResult.sidedSuccess(false);
            }
            else if (stack.is(Items.NETHERITE_SCRAP)) {
                if (!player.isCreative()) stack.shrink(1);
                this.entityData.set(PAYMENT_STATE, 2);
                this.playSound(SoundEvents.VILLAGER_YES, 1.0F, this.getVoicePitch());
                return InteractionResult.sidedSuccess(false);
            }
        }
        // --- ЛОГИКА 2: ВЗАИМОДЕЙСТВИЕ С ИНСТРУМЕНТОМ ---
        else {
            // Если Кузнец готов РАЗБИРАТЬ (оплачен золотом)
            if (state == 1) {
                ItemStack result = getDisassemblyResult(stack);
                if (!result.isEmpty()) {
                    if (!player.isCreative()) stack.shrink(1); // Забираем инструмент

                    // Выдаем ресурсы
                    dropItemToPlayer(player, result);

                    // Звуки переплавки и партиклы
                    this.level().playSound(null, this.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    this.level().playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.5F, 1.0F);
                    spawnFlameParticles();

                    this.entityData.set(PAYMENT_STATE, 0); // Сбрасываем состояние оплаты
                    return InteractionResult.sidedSuccess(false);
                }
            }
            // Если Кузнец готов ЧИНИТЬ (оплачен незеритом)
            else if (state == 2) {
                // Проверяем, что это инструмент (имеет прочность), но НЕ броня
                if (stack.isDamageableItem() && !(stack.getItem() instanceof ArmorItem)) {
                    ItemStack repaired = stack.copy();
                    repaired.setDamageValue(0); // Полностью чиним

                    if (!player.isCreative()) stack.shrink(1); // Забираем сломанный

                    dropItemToPlayer(player, repaired); // Отдаем починенный с чарами

                    // Звук кузнеца и партиклы
                    this.level().playSound(null, this.blockPosition(), SoundEvents.VILLAGER_WORK_WEAPONSMITH, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    spawnFlameParticles();

                    this.entityData.set(PAYMENT_STATE, 0);
                    return InteractionResult.sidedSuccess(false);
                }
            }
        }

        // Если игрок кликнул чем-то другим или не тем предметом -> ПОКАЗЫВАЕМ ПОДСКАЗКУ
        this.entityData.set(SHOW_HINT, true);
        this.hintTimer = 80;
        this.playSound(SoundEvents.VILLAGER_NO, 1.0F, this.getVoicePitch());
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5D, this.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
        }

        return InteractionResult.sidedSuccess(false);
    }

    // --- ЛОГИКА РАЗБОРА ИНСТРУМЕНТА ---
    private ItemStack getDisassemblyResult(ItemStack tool) {
        if (!(tool.getItem() instanceof TieredItem tieredItem)) return ItemStack.EMPTY;

        Tier tier = tieredItem.getTier();
        if (tier == Tiers.WOOD || tier == Tiers.STONE) return ItemStack.EMPTY; // Дерево и камень не разбираем

        Item material = Items.IRON_INGOT;
        if (tier == Tiers.GOLD) material = Items.GOLD_INGOT;
        else if (tier == Tiers.DIAMOND) material = Items.DIAMOND;
        else if (tier == Tiers.NETHERITE) material = Items.NETHERITE_INGOT;

        int count = 1;
        Item item = tool.getItem();
        if (item instanceof PickaxeItem || item instanceof AxeItem) count = 3;
        else if (item instanceof SwordItem || item instanceof HoeItem) count = 2;
        else if (item instanceof ShovelItem) count = 1;
        else return ItemStack.EMPTY;

        return new ItemStack(material, count);
    }

    private void dropItemToPlayer(Player player, ItemStack stack) {
        Vec3 dir = player.position().subtract(this.position()).normalize().scale(0.3);
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 1.0D, this.getZ(), stack);
        itemEntity.setDeltaMovement(dir.x, 0.3D, dir.z);
        this.level().addFreshEntity(itemEntity);
    }

    private void spawnFlameParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0D, this.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            // 1. Таймер подсказки
            if (this.hintTimer > 0) {
                this.hintTimer--;
                if (this.hintTimer <= 0) this.entityData.set(SHOW_HINT, false);
            }

            // 2. Ищем наковальню (раз в секунду проверяем её наличие)
            if (this.tickCount % 20 == 0) {
                // Если наковальня была, но ее сломали — сбрасываем цель
                if (this.targetAnvil != null && !(this.level().getBlockState(this.targetAnvil).getBlock() instanceof AnvilBlock)) {
                    this.targetAnvil = null;
                }

                // Если цели нет — сканируем территорию
                if (this.targetAnvil == null) {
                    for (BlockPos pos : BlockPos.betweenClosed(this.blockPosition().offset(-3, -1, -3), this.blockPosition().offset(3, 1, 3))) {
                        if (this.level().getBlockState(pos).getBlock() instanceof AnvilBlock) {
                            this.targetAnvil = pos;
                            break; // Нашли - выходим из цикла
                        }
                    }
                }
            }

            // 3. ЖЕСТКАЯ ФИКСАЦИЯ ПОВОРОТА (Каждый тик!)
            if (this.targetAnvil != null) {
                // Высчитываем точный угол от кузнеца до центра наковальни
                double dx = this.targetAnvil.getX() + 0.5D - this.getX();
                double dz = this.targetAnvil.getZ() + 0.5D - this.getZ();
                float targetYaw = (float) (Math.atan2(-dx, dz) * (180D / Math.PI));

                // Принудительно блокируем все оси вращения
                this.setYRot(targetYaw);
                this.setYHeadRot(targetYaw);
                this.yBodyRot = targetYaw;
            }
        }
    }

    // --- ЗВУКИ ПИЛЛАДЖЕРА И КАСТОМНЫЙ ЭМБИЕНТ ---
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BLACKSMITH_IDLE.get();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400; // 20 секунд (20 тиков * 20)
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    // --- СОХРАНЕНИЕ ---
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PaymentState", this.entityData.get(PAYMENT_STATE));
    }

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(PAYMENT_STATE, tag.getInt("PaymentState"));
    }

    // --- АНИМАЦИИ ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            return event.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}