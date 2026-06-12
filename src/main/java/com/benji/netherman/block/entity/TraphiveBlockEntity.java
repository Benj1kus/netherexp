package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.TraphiveBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TraphiveBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final int STATE_CLOSED = 0;
    public static final int STATE_OPENING = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_CLOSING = 3;

    private static final RawAnimation ANIM_OPEN = RawAnimation.begin().thenPlayAndHold("open");
    private static final RawAnimation ANIM_IDLE_OPEN = RawAnimation.begin().thenLoop("open_idle");
    private static final RawAnimation ANIM_CLOSE = RawAnimation.begin().thenPlayAndHold("close");
    private static final RawAnimation ANIM_IDLE_CLOSE = RawAnimation.begin().thenLoop("close_idle");

    private int blockStateAnim = STATE_CLOSED;
    private boolean isWaitingToOpen = false;
    private int delayTimer = 0;
    private int animTimer = 0;
    private int autoCloseTimer = 0;
    private int contactTimer = 0;

    public TraphiveBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.TRAPHIVE_BE.get(), pos, state);
    }

    public int getBlockStateAnim() { return blockStateAnim; }

    public void setBlockStateAnim(int state) {
        this.blockStateAnim = state;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void triggerWave(int distance, int maxDistance) {
        if (this.blockStateAnim != STATE_CLOSED) return;

        this.delayTimer = (int) (Math.pow(distance, 0.8) * 3);
        this.isWaitingToOpen = true;

        int reverseDistance = maxDistance - distance;
        this.autoCloseTimer = 100 + (int) (Math.pow(reverseDistance, 0.8) * 3);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TraphiveBlockEntity entity) {
        if (!level.isClientSide()) {
            if (entity.blockStateAnim == STATE_CLOSED && !entity.isWaitingToOpen) {
                AABB touchBox = new AABB(pos).inflate(0.05D);
                boolean isTouching = false;

                for (Player player : level.players()) {
                    if (player.isAlive() && player.getBoundingBox().intersects(touchBox)) {
                        isTouching = true;
                        break;
                    }
                }

                if (isTouching) {
                    entity.contactTimer++;
                    if (entity.contactTimer >= 10) {
                        if (state.getBlock() instanceof TraphiveBlock block) {
                            block.activateWave(level, pos);
                        }
                        entity.contactTimer = 0;
                    }
                } else {
                    if (entity.contactTimer > 0) entity.contactTimer--;
                }
            }
            if (entity.isWaitingToOpen) {
                entity.delayTimer--;
                if (entity.delayTimer <= 0) {
                    entity.isWaitingToOpen = false;
                    entity.setBlockStateAnim(STATE_OPENING);
                    entity.animTimer = 20;

                    level.playSound(null, pos, SoundEvents.SHROOMLIGHT_BREAK, SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.OPEN, true));
                }
            }
            else if (entity.blockStateAnim == STATE_OPENING) {
                entity.animTimer--;
                if (entity.animTimer <= 0) entity.setBlockStateAnim(STATE_OPEN);
            }
            else if (entity.blockStateAnim == STATE_OPEN) {
                entity.autoCloseTimer--;
                if (entity.autoCloseTimer <= 0) {
                    entity.setBlockStateAnim(STATE_CLOSING);
                    entity.animTimer = 20;

                    level.playSound(null, pos, SoundEvents.SHROOMLIGHT_BREAK, SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
                }
            }
            else if (entity.blockStateAnim == STATE_CLOSING) {
                entity.animTimer--;
                if (entity.animTimer <= 0) {
                    entity.setBlockStateAnim(STATE_CLOSED);
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.OPEN, false));
                }
            }
        } else {
            if (entity.blockStateAnim == STATE_OPENING || entity.blockStateAnim == STATE_CLOSING) {
                for (int i = 0; i < 2; i++) {
                    double px = pos.getX() + level.random.nextDouble();
                    double py = pos.getY() + level.random.nextDouble();
                    double pz = pos.getZ() + level.random.nextDouble();

                    level.addParticle(DustParticleOptions.REDSTONE, px, py, pz, 0, 0.05D, 0);
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            return switch (this.blockStateAnim) {
                case STATE_OPENING -> event.setAndContinue(ANIM_OPEN);
                case STATE_OPEN -> event.setAndContinue(ANIM_IDLE_OPEN);
                case STATE_CLOSING -> event.setAndContinue(ANIM_CLOSE);
                default -> event.setAndContinue(ANIM_IDLE_CLOSE);
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("BlockStateAnim", this.blockStateAnim);
        tag.putBoolean("Waiting", this.isWaitingToOpen);
        tag.putInt("DelayTimer", this.delayTimer);
        tag.putInt("AnimTimer", this.animTimer);
        tag.putInt("AutoClose", this.autoCloseTimer);
        tag.putInt("ContactTimer", this.contactTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.blockStateAnim = tag.getInt("BlockStateAnim");
        this.isWaitingToOpen = tag.getBoolean("Waiting");
        this.delayTimer = tag.getInt("DelayTimer");
        this.animTimer = tag.getInt("AnimTimer");
        this.autoCloseTimer = tag.getInt("AutoClose");
        this.contactTimer = tag.getInt("ContactTimer");
    }

    @Override
    public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) this.load(pkt.getTag());
    }
}