package com.benji.netherman.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.MazeDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BellGuardianEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int soundTimer = 0;
    private int stuckTimer = 0;

    public BellGuardianEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 1024.0D);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && this.isAlive()) {

            boolean isMovingOrAggressive = this.getDeltaMovement().horizontalDistanceSqr() > 0.001D || this.getTarget() != null;
            if (isMovingOrAggressive) {
                soundTimer++;
                if (soundTimer >= 52) {
                    playRandomBeastSound();
                    soundTimer = 0;
                }
            } else {
                soundTimer = 0;
            }

            if (this.getTarget() != null) {
                Player target = (Player) this.getTarget();

                this.getLookControl().setLookAt(target, 30.0F, 30.0F);

                if (this.distanceTo(target) > 1.5D) {
                    this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
                }

                if (this.horizontalCollision) {
                    stuckTimer++;
                    if (stuckTimer >= 20) {
                        smashObstaclesInFront();
                        stuckTimer = 0;
                    }
                } else {
                    stuckTimer = 0;
                }
            }
        }
    }

    private void smashObstaclesInFront() {
        Direction facing = this.getDirection();
        BlockPos center = this.blockPosition().relative(facing).above(1);

        boolean brokeSomething = false;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p;
                    if (facing.getAxis() == Direction.Axis.Z) {
                        p = center.offset(x, y, 0);
                    } else {
                        p = center.offset(0, y, z);
                    }

                    BlockState state = this.level().getBlockState(p);

                    if (state.isAir() || state.getDestroySpeed(this.level(), p) < 0) continue;

                    if (state.is(NetherExp.MAZE_DOOR.get())) {
                        if (this.level().getBlockEntity(p) instanceof MazeDoorBlockEntity door) {
                            door.openTemporary();
                        }
                    }
                    else if (state.is(NetherExp.GRAND_DOOR_PART.get())) {
                        openMazeDoorSafely(p);
                    }
                    else {
                        if (ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
                            this.level().destroyBlock(p, true);
                            brokeSomething = true;
                        }
                    }
                }
            }
        }

        if (brokeSomething) {
            this.playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 2.0F, 0.5F);
        }
    }

    private void openMazeDoorSafely(BlockPos partPos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(partPos.offset(-5, -6, -5), partPos.offset(5, 6, 5))) {
            if (this.level().getBlockState(checkPos).is(NetherExp.MAZE_DOOR.get())) {
                if (this.level().getBlockEntity(checkPos) instanceof MazeDoorBlockEntity door) {
                    door.openTemporary();
                    return;
                }
            }
        }
    }

    private void playRandomBeastSound() {
        SoundEvent[] sounds = {
                ModSounds.BELL_BEAST_1.get(),
                ModSounds.BELL_BEAST_3.get(),
                ModSounds.BELL_BEAST_4.get(),
                ModSounds.BELL_BEAST_5.get(),
                ModSounds.BELL_BEAST_6.get(),
                ModSounds.BELL_BEAST_7.get(),
                ModSounds.BELL_BEAST_8.get(),
                ModSounds.BELL_BEAST_9.get(),
        };
        SoundEvent randomSound = sounds[this.random.nextInt(sounds.length)];
        this.playSound(randomSound, 1.0F, 1.0F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}