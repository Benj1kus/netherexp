package com.benji.netherman.block.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.block.FacePuzzleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FacePuzzleBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int clickState = 0;
    private boolean isSolved = false;

    private static final RawAnimation ANIM_INCORRECT_IDLE = RawAnimation.begin().thenLoop("incorrect_idle");
    private static final RawAnimation ANIM_CLICK_1 = RawAnimation.begin().thenPlayAndHold("click_1");
    private static final RawAnimation ANIM_CLICK_2 = RawAnimation.begin().thenPlayAndHold("click_2");
    private static final RawAnimation ANIM_CLICK_CORRECT = RawAnimation.begin().thenPlayAndHold("click_correct");

    public FacePuzzleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getClickState() { return this.clickState; }
    public boolean isSolved() { return this.isSolved; }

    public void advanceState() {
        if (this.isSolved || this.level == null || this.level.isClientSide()) return;

        BlockState state = this.getBlockState();
        int maxStates = state.getValue(FacePuzzleBlock.MAX_STATES);

        this.clickState++;
        if (this.clickState >= maxStates) {
            this.clickState = 0;
        }

        this.level.playSound(null, this.worldPosition, ModSounds.HIRRING.get(), SoundSource.BLOCKS, 1.0F, 0.8F);

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

        checkWinCondition();
    }

    private void checkWinCondition() {
        if (this.level == null) return;

        int myMaxStates = this.getBlockState().getValue(FacePuzzleBlock.MAX_STATES);
        if (this.clickState != myMaxStates - 1) return;

        int correctPiecesCount = 0;
        int searchRadius = 3;

        for (BlockPos checkPos : BlockPos.betweenClosed(worldPosition.offset(-searchRadius, -searchRadius, -searchRadius), worldPosition.offset(searchRadius, searchRadius, searchRadius))) {
            if (level.getBlockEntity(checkPos) instanceof FacePuzzleBlockEntity entity) {
                int hisMaxStates = entity.getBlockState().getValue(FacePuzzleBlock.MAX_STATES);
                if (entity.getClickState() == hisMaxStates - 1 && !entity.isSolved()) {
                    correctPiecesCount++;
                }
            }
        }

        if (correctPiecesCount == 4) {
            triggerWin();
        }
    }

    private void triggerWin() {
        if (this.level == null) return;

        int searchRadius = 3;
        for (BlockPos checkPos : BlockPos.betweenClosed(worldPosition.offset(-searchRadius, -searchRadius, -searchRadius), worldPosition.offset(searchRadius, searchRadius, searchRadius))) {
            if (level.getBlockEntity(checkPos) instanceof FacePuzzleBlockEntity entity) {
                int hisMaxStates = entity.getBlockState().getValue(FacePuzzleBlock.MAX_STATES);
                if (entity.getClickState() == hisMaxStates - 1 && !entity.isSolved()) {
                    entity.isSolved = true;
                    entity.setChanged();
                    level.sendBlockUpdated(entity.worldPosition, entity.getBlockState(), entity.getBlockState(), 3);
                }
            }
        }

        level.playSound(null, this.worldPosition, ModSounds.GIANT_BELL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                    100, 0.5, 0.5, 0.5, 0.2);
        }

        ItemEntity key = new ItemEntity(level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, new ItemStack(NetherExp.MAZE_KEY.get()));
        level.addFreshEntity(key);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {

            if (this.isSolved) {
                return event.setAndContinue(ANIM_CLICK_CORRECT);
            }

            int maxStates = this.getBlockState().getValue(FacePuzzleBlock.MAX_STATES);

            if (maxStates == 2) {
                if (this.clickState == 1) return event.setAndContinue(ANIM_CLICK_CORRECT);
                return event.setAndContinue(ANIM_INCORRECT_IDLE);
            }
            else if (maxStates == 3) {
                if (this.clickState == 1) return event.setAndContinue(ANIM_CLICK_1);
                if (this.clickState == 2) return event.setAndContinue(ANIM_CLICK_CORRECT);
                return event.setAndContinue(ANIM_INCORRECT_IDLE);
            }

            return event.setAndContinue(ANIM_INCORRECT_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ClickState", this.clickState);
        tag.putBoolean("IsSolved", this.isSolved);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.clickState = tag.getInt("ClickState");
        this.isSolved = tag.getBoolean("IsSolved");
    }

    @Override
    public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) this.load(pkt.getTag());
    }
}