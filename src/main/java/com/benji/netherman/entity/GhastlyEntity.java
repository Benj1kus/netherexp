package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhastlyEntity extends TamableAnimal implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Переменные для луча-компаса
    private int mansionBeamTicks = 0;
    private BlockPos targetMansionPos = null;

    // Синхронизация состояний
    public static final EntityDataAccessor<Boolean> IS_EATING = SynchedEntityData.defineId(GhastlyEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SHOW_HINT = SynchedEntityData.defineId(GhastlyEntity.class, EntityDataSerializers.BOOLEAN);

    public int hintTicks = 0; // Таймер для подсказки
    public int eatTicks = 0; // Таймер для поедания корней

    public GhastlyEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_EATING, false);
        this.entityData.define(SHOW_HINT, false);
    }

    // Звук получения урона
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return this.random.nextBoolean() ? ModSounds.GHASTLY_HURT_1.get() : ModSounds.GHASTLY_HURT_3.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        // Проверяем, не занят ли моб поеданием (возможно, с набитым ртом он молчит)
        if (!this.entityData.get(IS_EATING)) {
            return ModSounds.GHASTLY_IDLE.get();
        }
        return null;
    }

    // 2. Рандомизируем высоту (питч) ВСЕХ звуков моба (и эмбиента, и урона)
    @Override
    public float getVoicePitch() {
        // Базовый питч + рандомное отклонение.
        // 0.8F + от 0.0F до 0.4F = от 0.8F до 1.2F
        return 0.8F + this.random.nextFloat() * 0.4F;
    }


    // Включаем навигацию для летающих мобов
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D, 5.0F, 1.0F, false));
        this.goalSelector.addGoal(2, new GhastlyBuildNestGoal(this)); // Приоритет 2 (Высокий!)
        this.goalSelector.addGoal(3, new GhastlyPollinateGoal(this)); // Приоритет 3
        this.goalSelector.addGoal(4, new GhastlyEnterHiveGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
    }


    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
        }

        if (this.isTame()) {
            // Если игрок кликает багровыми корнями по УЖЕ прирученному мобу
            if (itemstack.is(Blocks.CRIMSON_ROOTS.asItem())) {
                if (!player.getAbilities().instabuild) itemstack.shrink(1);

                // Звук отрыжки
                this.playSound(SoundEvents.PLAYER_BURP, 1.0F, this.getVoicePitch());

                if (this.level() instanceof ServerLevel serverLevel) {
                    // Партиклы редстоуна вокруг моба
                    DustParticleOptions redstoneExplosion = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.5f);
                    serverLevel.sendParticles(redstoneExplosion, this.getX(), this.getY() + 0.5, this.getZ(), 20, 0.3, 0.3, 0.3, 0.0);

                    // Поиск структуры
                    Registry<Structure> registry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
                    Holder<Structure> mansionHolder = registry.getHolder(ResourceKey.create(Registries.STRUCTURE, new ResourceLocation("netherman", "mansion_nether"))).orElse(null);

                    if (mansionHolder != null) {
                        // Ищем структуру в радиусе 100 чанков (1600 блоков)
                        Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(
                                serverLevel, HolderSet.direct(mansionHolder), this.blockPosition(), 100, false
                        );

                        if (pair != null) {
                            this.targetMansionPos = pair.getFirst();
                            this.mansionBeamTicks = 100; // Луч будет гореть ровно 5 секунд (100 тиков)
                        } else {
                            // Если особняк не найден поблизости, пускаем дым
                            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 10, 0.2, 0.2, 0.2, 0.0);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        } else {
            // Проверяем нужный предмет
            if (itemstack.is(Blocks.CRIMSON_ROOTS.asItem()) || itemstack.is(Blocks.WARPED_ROOTS.asItem())) {
                if (!player.getAbilities().instabuild) itemstack.shrink(1);

                this.tame(player);
                this.level().broadcastEntityEvent(this, (byte) 7); // Зеленые сердечки
                return InteractionResult.SUCCESS;
            } else {
                // Игрок кликнул чем-то другим - показываем подсказку!
                this.entityData.set(SHOW_HINT, true);
                this.hintTicks = 60; // Подсказка висит 3 секунды

                // Партиклы дыма
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.2, 0.2, 0.2, 0.0);
                }
                return InteractionResult.SUCCESS;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.hintTicks > 0) {
                this.hintTicks--;
                if (this.hintTicks == 0) this.entityData.set(SHOW_HINT, false);
            }
            if (this.eatTicks > 0) {
                this.eatTicks--;
                if (this.eatTicks == 0) this.entityData.set(IS_EATING, false);
            }

            // Отрисовка луча к особняку
            if (this.mansionBeamTicks > 0 && this.targetMansionPos != null) {
                this.mansionBeamTicks--;

                // Спавним частицы каждые 2 тика, чтобы не перегружать клиент
                if (this.mansionBeamTicks % 2 == 0) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    Vec3 start = this.position().add(0, this.getBbHeight() / 2.0, 0);
                    Vec3 target = Vec3.atCenterOf(this.targetMansionPos);
                    Vec3 direction = target.subtract(start).normalize();

                    // Масштаб частиц уменьшается по мере истечения времени (плавное затухание)
                    float particleScale = Math.max(0.1f, (this.mansionBeamTicks / 100.0f) * 1.5f);
                    DustParticleOptions beamParticle = new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), particleScale);

                    // Рисуем луч длиной 12 блоков
                    for (int i = 1; i <= 12; i++) {
                        Vec3 particlePos = start.add(direction.scale(i));
                        serverLevel.sendParticles(beamParticle, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }
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

    // Отключаем урон от падения
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) { return null; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            if (this.entityData.get(SHOW_HINT)) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("idle_bite"));
            }
            if (this.entityData.get(IS_EATING)) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("idle_bite"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}