package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.AltarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AltarBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private BlockPos targetPuzzlePos = null;
    private boolean needsSearch = false;
    private int searchLetter = 0;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.ALTAR_BE.get(), pos, state);
    }

    public void triggerPuzzleSearch(int letter) {
        this.needsSearch = true;
        this.searchLetter = letter;
    }

    public BlockPos getTargetPuzzlePos() {
        return targetPuzzlePos;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AltarBlockEntity entity) {
        if (state.getValue(AltarBlock.LIT)) {

            if (level.isClientSide() && level.random.nextInt(8) == 0) {
                level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 0, 0.02, 0);
            }

            if (!level.isClientSide() && state.getValue(AltarBlock.ACTIVE)) {
                if (entity.needsSearch) {
                    entity.performSearch(level, pos);
                }

                if (entity.targetPuzzlePos != null) {
                    if (level.getBlockState(entity.targetPuzzlePos).isAir()) {
                        entity.targetPuzzlePos = null;
                    } else {
                        ((ServerLevel) level).sendParticles(ParticleTypes.FLAME,
                                entity.targetPuzzlePos.getX() + 0.5,
                                entity.targetPuzzlePos.getY() + 0.5,
                                entity.targetPuzzlePos.getZ() + 0.5,
                                2, 0.2, 0.2, 0.2, 0.01);
                    }
                }
            }
        }
    }

    // Теперь метод public
    public void performSearch(Level level, BlockPos center) {
        this.needsSearch = false;
        Block targetBlock = getTargetBlockForLetter(this.searchLetter);
        if (targetBlock == null) return;

        for (int x = -20; x <= 20; x++) {
            for (int y = -20; y <= 20; y++) {
                for (int z = -20; z <= 20; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    if (level.getBlockState(checkPos).is(targetBlock)) {
                        this.targetPuzzlePos = checkPos;
                        return;
                    }
                }
            }
        }
    }

    private Block getTargetBlockForLetter(int letter) {
        return switch (letter) {
            case 1 -> NetherExp.A_PUZZLE.get();
            case 5 -> NetherExp.E_PUZZLE.get();
            case 12 -> NetherExp.L_PUZZLE.get();
            case 26 -> NetherExp.Z_PUZZLE.get();
            default -> null;
        };
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            BlockState state = getBlockState();

            if (state.hasProperty(AltarBlock.GUESSED) && state.getValue(AltarBlock.GUESSED)) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("idle_off"));
            }

            if (state.hasProperty(AltarBlock.LIT)) {
                if (!state.getValue(AltarBlock.LIT)) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("idle_off"));
                }
                if (state.getValue(AltarBlock.LETTER) == 0) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("idle_on"));
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("idle_choose"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle_off"));
        }));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.targetPuzzlePos != null) {
            tag.putLong("PuzzlePos", this.targetPuzzlePos.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("PuzzlePos")) {
            this.targetPuzzlePos = BlockPos.of(tag.getLong("PuzzlePos"));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}