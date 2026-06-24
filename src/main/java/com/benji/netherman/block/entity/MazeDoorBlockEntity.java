package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.MazeDoorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import com.benji.netherman.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MazeDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final int STATE_CLOSED = 0;
    public static final int STATE_OPENING = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_CLOSING = 3;

    private static final RawAnimation ANIM_OPEN = RawAnimation.begin().thenPlayAndHold("open");
    private static final RawAnimation ANIM_IDLE_OPEN = RawAnimation.begin().thenLoop("idle_open");
    private static final RawAnimation ANIM_CLOSE = RawAnimation.begin().thenPlayAndHold("close");
    private static final RawAnimation ANIM_IDLE_CLOSE = RawAnimation.begin().thenLoop("idle_close");

    private int doorState = STATE_CLOSED;
    private int animTimer = 0;
    private int autoCloseTimer = 0;

    public boolean requiresKey = false;

    public MazeDoorBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.MAZE_DOOR_BE.get(), pos, state);
    }

    public int getDoorState() { return doorState; }

    public void setDoorState(int state) {
        this.doorState = state;
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            boolean isOpen = (state == STATE_OPEN || state == STATE_OPENING);
            BlockState mainState = this.getBlockState();

            if (state == STATE_OPENING) {
                this.level.playSound(null, this.worldPosition, ModSounds.GRAND_DOOR_OPEN.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
            } else if (state == STATE_CLOSING) {
                this.level.playSound(null, this.worldPosition, ModSounds.GRAND_DOOR_CLOSE.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
            }

            if (mainState.hasProperty(BlockStateProperties.OPEN) && mainState.getValue(BlockStateProperties.OPEN) != isOpen) {
                this.level.setBlock(this.worldPosition, mainState.setValue(BlockStateProperties.OPEN, isOpen), 3);

                Direction right = mainState.getValue(MazeDoorBlock.FACING).getClockWise();
                for (int w = 0; w < 6; w++) {
                    for (int h = 0; h < 5; h++) {
                        if (w == 0 && h == 0) continue;
                        BlockPos p = this.worldPosition.relative(right, w).above(h);
                        BlockState st = this.level.getBlockState(p);

                        if (st.is(NetherExp.GRAND_DOOR_PART.get()) && st.hasProperty(BlockStateProperties.OPEN) && st.getValue(BlockStateProperties.OPEN) != isOpen) {
                            this.level.setBlock(p, st.setValue(BlockStateProperties.OPEN, isOpen), 3);
                        }
                    }
                }
            }
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void openTemporary() {
        if (this.doorState == STATE_CLOSED) {
            this.setDoorState(STATE_OPENING);
            this.animTimer = 35;
            this.autoCloseTimer = 200;
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MazeDoorBlockEntity entity) {
        if (!level.isClientSide()) {
            if (entity.doorState == STATE_OPENING) {
                entity.animTimer--;
                if (entity.animTimer <= 0) entity.setDoorState(STATE_OPEN);
            }
            else if (entity.doorState == STATE_CLOSING) {
                entity.animTimer--;
                if (entity.animTimer <= 0) entity.setDoorState(STATE_CLOSED);
            }
            else if (entity.doorState == STATE_OPEN) {
                if (entity.autoCloseTimer > 0) {
                    entity.autoCloseTimer--;
                } else {
                    entity.setDoorState(STATE_CLOSING);
                    entity.animTimer = 35;
                }
            }
        }
        else {
            if (entity.doorState == STATE_OPENING || entity.doorState == STATE_CLOSING) {
                Direction facing = state.getValue(MazeDoorBlock.FACING);
                Direction right = facing.getClockWise();

                for (int i = 0; i < 30; i++) {
                    double w = level.random.nextDouble() * 6.0;
                    double h = level.random.nextDouble() * 5.0;

                    double px = pos.getX() + 0.5 + right.getStepX() * w;
                    double py = pos.getY() + h;
                    double pz = pos.getZ() + 0.5 + right.getStepZ() * w;

                    level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE_BRICKS.defaultBlockState()),
                            px, py, pz,
                            (level.random.nextDouble() - 0.5) * 0.3,
                            level.random.nextDouble() * 0.4,
                            (level.random.nextDouble() - 0.5) * 0.3);
                }

                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    double distSq = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                    if (distSq <= 200.0) { // Радиус тряски поменьше, чем у Гранд Двери
                        float intensity = (float) (1.0 - (distSq / 200.0)) * 2.0F;
                        player.setXRot(player.getXRot() + (level.random.nextFloat() - 0.5F) * intensity);
                        player.setYRot(player.getYRot() + (level.random.nextFloat() - 0.5F) * intensity);
                    }
                }
            }
        }
    }

    public void syncRequiresKey(boolean requiresKey) {
        this.requiresKey = requiresKey;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            return switch (this.doorState) {
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
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(this.getBlockPos()).inflate(6.0D);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("DoorState", this.doorState);
        tag.putInt("AnimTimer", this.animTimer);
        tag.putInt("AutoClose", this.autoCloseTimer);
        tag.putBoolean("RequiresKey", this.requiresKey);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.doorState = tag.getInt("DoorState");
        this.animTimer = tag.getInt("AnimTimer");
        this.autoCloseTimer = tag.getInt("AutoClose");
        this.requiresKey = tag.getBoolean("RequiresKey");
    }

    @Override
    public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            this.load(pkt.getTag());
        }
    }
}