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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
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

import java.util.List;
import java.util.UUID;

public class GildedGolemEntity extends IronGolem implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 = Full (100-50%), 1 = Damaged (50-25%), 2 = Healing (<25%)
    public static final EntityDataAccessor<Integer> TEXTURE_STATE = SynchedEntityData.defineId(GildedGolemEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID creatorUUID;
    private int attackTimer = 0;
    private boolean isHealingPhase = false;

    public GildedGolemEntity(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D); // Урон как у обычного голема
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TEXTURE_STATE, 0);
    }

    public void setCreatorUUID(@Nullable UUID uuid) {
        this.creatorUUID = uuid;
    }

    @Nullable
    public UUID getCreatorUUID() {
        return this.creatorUUID;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // 1. Атакует тех, кто ударил его (исключая создателя)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());

        // 2. ИЗМЕНЕНО: Атакует ТОЛЬКО мирных мобов (Животных и Жителей), игнорируя монстров
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false,
                (entity) -> entity instanceof net.minecraft.world.entity.animal.Animal || entity instanceof net.minecraft.world.entity.animal.IronGolem || entity instanceof net.minecraft.world.entity.npc.AbstractVillager));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (this.attackTimer > 0) {
                this.attackTimer--;
            }
        } else {
            float healthPct = this.getHealth() / this.getMaxHealth();

            // КРИТИЧЕСКИЙ ФИКС: Если голем полностью здоров — принудительно отключаем всасывание
            if (this.getHealth() >= this.getMaxHealth()) {
                this.isHealingPhase = false;
            }

            // Включаем режим лечения, если ХП упало ниже 25%
            if (healthPct <= 0.25F && !this.isHealingPhase && this.getHealth() > 0) {
                this.isHealingPhase = true;
            }

            // Обработка фаз и текстур
            if (this.isHealingPhase) {
                this.entityData.set(TEXTURE_STATE, 2);
                boolean stillSucking = performHealingSuck();

                // Если все предметы засосались и игроки умерли/убежали — отключаем фазу
                if (!stillSucking) {
                    this.isHealingPhase = false;
                }
            } else if (healthPct <= 0.5F) {
                this.entityData.set(TEXTURE_STATE, 1);
            } else {
                this.entityData.set(TEXTURE_STATE, 0);
            }

            // Механика защиты Believer (Поиск обидчиков)
            if (this.tickCount % 10 == 0 && this.getTarget() == null && !this.isHealingPhase) {
                List<BelieverEntity> believers = this.level().getEntitiesOfClass(BelieverEntity.class, this.getBoundingBox().inflate(15.0D));
                for (BelieverEntity believer : believers) {
                    LivingEntity attacker = believer.getLastHurtByMob();
                    if (attacker != null && attacker.isAlive()) {
                        if (this.creatorUUID != null && attacker.getUUID().equals(this.creatorUUID)) {
                            continue;
                        }
                        this.setTarget(attacker);
                        break;
                    }
                }
            }
        }
    }

    // --- ЛОГИКА ВСАСЫВАНИЯ (HEALING) ---
    private boolean performHealingSuck() {
        // Проверка на случай полного исцеления прямо перед тиком всасывания
        if (this.getHealth() >= this.getMaxHealth()) {
            return false;
        }

        List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(10.0D));
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0D));

        // Фильтруем игроков: убираем креатив, спектральный режим, мертвых и создателя голема
        players.removeIf(p -> p.isCreative() || p.isSpectator() || !p.isAlive() || (this.creatorUUID != null && p.getUUID().equals(this.creatorUUID)));

        // Если предметов нет и живых игроков в радиусе тоже нет — останавливаем "пылесос"
        if (items.isEmpty() && players.isEmpty()) {
            return false;
        }

        // Звуки ветра и дыхания (каждые 20 тиков, когда метод работает)
        if (this.tickCount % 20 == 0) {
            this.playSound(SoundEvents.BLAZE_AMBIENT, 1.0F, 0.5F);
            this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.0F, 1.5F);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 3, 0.5, 0.5, 0.5, 0.05);
        }

        boolean ateItem = false;

        // 1. Сначала поглощаем предметы на земле
        for (ItemEntity item : items) {
            pullEntity(item);
            if (this.distanceToSqr(item) < 2.5D) {
                item.discard();
                this.heal(4.0F); // Восстановление ХП от предметов
                this.playSound(SoundEvents.WITCH_DRINK, 1.0F, 1.0F);
                ateItem = true;
                break; // За один тик съедаем один предмет для плавности звуков
            }
        }

        // 2. Если предметов в этом тике не поглотили — переключаемся на высасывание ХП из игроков
        if (!ateItem && !players.isEmpty()) {
            for (Player player : players) {
                pullEntity(player);
                if (this.distanceToSqr(player) < 3.5D) {
                    if (this.tickCount % 20 == 0) { // Каждую секунду (глоток)

                        // ФИКС: Используем урон MAGIC вместо mobAttack, чтобы обойти командную защиту голема!
                        player.hurt(this.damageSources().magic(), 2.0F); // Наносим игроку 2 единицы урона (1 сердце)
                        this.heal(6.0F); // Лечим голема на 6 ХП за глоток

                        this.playSound(SoundEvents.WITCH_DRINK, 1.0F, 1.0F);
                    }
                }
            }
        }

        // Повторная проверка здоровья в конце тика
        if (this.getHealth() >= this.getMaxHealth()) {
            return false;
        }

        return true; // Всасывание продолжается
    }

    private void pullEntity(Entity entity) {
        Vec3 pullVec = this.position().subtract(entity.position()).normalize().scale(0.15D); // Сила притяжения
        entity.setDeltaMovement(entity.getDeltaMovement().add(pullVec.x, 0.0D, pullVec.z));
        entity.hurtMarked = true;
    }

    // --- ЛОГИКА АТАКИ (Подкидывание) ---
    @Override
    public boolean doHurtTarget(Entity entity) {
        this.attackTimer = 10; // 0.5 секунды анимации
        this.level().broadcastEntityEvent(this, (byte) 4); // Триггер анимации на клиент

        boolean flag = super.doHurtTarget(entity);
        if (flag) {
            float upKnockback = 0.4F; // Подкидывание вверх
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, (double) upKnockback, 0.0D));
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        }
        return flag;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            this.attackTimer = 10;
        } else {
            super.handleEntityEvent(id);
        }
    }

    // Звуки урона и смерти ванильного голема
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.BLAZE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    // Дроп золотого блока (Дроп также нужно будет настроить через LootTables, но можно принудительно)
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(net.minecraft.world.item.Items.GOLD_BLOCK);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.creatorUUID != null) {
            tag.putUUID("Creator", this.creatorUUID);
        }
        tag.putBoolean("IsHealingPhase", this.isHealingPhase);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Creator")) {
            this.creatorUUID = tag.getUUID("Creator");
        }
        this.isHealingPhase = tag.getBoolean("IsHealingPhase");
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    // --- АНИМАЦИИ ---

    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            if (this.attackTimer > 0) {
                return event.setAndContinue(ATTACK_ANIM);
            }
            if (event.isMoving()) {
                return event.setAndContinue(WALK_ANIM);
            }
            return event.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}