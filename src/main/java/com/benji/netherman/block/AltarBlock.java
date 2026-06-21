package com.benji.netherman.block;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.AltarBlockEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AltarBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final IntegerProperty LETTER = IntegerProperty.create("letter", 0, 26);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty GUESSED = BooleanProperty.create("guessed");

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public AltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(LETTER, 0)
                .setValue(ACTIVE, false)
                .setValue(GUESSED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, LETTER, ACTIVE, GUESSED, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(GUESSED)) {
            return InteractionResult.PASS;
        }

        if (!state.getValue(LIT)) {
            if (player.getItemInHand(hand).is(Items.FLINT_AND_STEEL)) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(LIT, true), 3);
                    player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        } else {
            if (state.getValue(ACTIVE)) {
                ItemStack handItem = player.getItemInHand(hand);
                if (isCorrectPuzzleItem(state.getValue(LETTER), handItem.getItem())) {
                    if (!level.isClientSide()) {
                        if (!player.isCreative()) handItem.shrink(1);

                        level.setBlock(pos, state.setValue(GUESSED, true).setValue(ACTIVE, false).setValue(LIT, false), 3);

                        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5F, 1.0F);
                        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.2F);

                        ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 25, 0.3, 0.3, 0.3, 0.05);
                        ((ServerLevel) level).sendParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.4, 0.4, 0.4, 0.1);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }

            if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
                int currentLetter = state.getValue(LETTER);
                int nextLetter = currentLetter >= 26 ? 1 : currentLetter + 1;

                BlockState newState = state.setValue(LETTER, nextLetter);
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.5F);

                newState = checkAzazelLogic(level, pos, newState);
                level.setBlock(pos, newState, 3);

                if (newState.getValue(ACTIVE) && !state.getValue(ACTIVE)) {
                    level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 2.0F, 1.0F);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof AltarBlockEntity altar) {
                        altar.triggerPuzzleSearch(nextLetter);
                        altar.performSearch(level, pos);

                        if (altar.getTargetPuzzlePos() != null) {
                            player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(altar.getTargetPuzzlePos()));
                        }
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    private boolean isCorrectPuzzleItem(int letter, Item item) {
        if (letter == 1 && item == NetherExp.A_PUZZLE_ITEM.get()) return true;
        if (letter == 5 && item == NetherExp.E_PUZZLE_ITEM.get()) return true;
        if (letter == 12 && item == NetherExp.L_PUZZLE_ITEM.get()) return true;
        if (letter == 26 && item == NetherExp.Z_PUZZLE_ITEM.get()) return true;
        return false;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide() && state.getValue(LIT) && !state.getValue(GUESSED)) {
            BlockState reCheckedState = checkAzazelLogic(level, pos, state);

            if (reCheckedState != state) {
                level.setBlock(pos, reCheckedState, 3);

                if (reCheckedState.getValue(ACTIVE) && !state.getValue(ACTIVE)) {
                    level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 2.0F, 1.0F);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof AltarBlockEntity altar) {
                        altar.triggerPuzzleSearch(reCheckedState.getValue(LETTER));
                    }
                }
            }
        }
    }

    private BlockState checkAzazelLogic(Level level, BlockPos pos, BlockState currentState) {
        int letter = currentState.getValue(LETTER);

        boolean hasZ = hasAdjacent(level, pos, 26, false) || hasAdjacent(level, pos, 26, true);
        boolean hasE = hasAdjacent(level, pos, 5, false) || hasAdjacent(level, pos, 5, true);
        boolean hasL = hasAdjacent(level, pos, 12, false) || hasAdjacent(level, pos, 12, true);
        boolean hasAActive = hasAdjacent(level, pos, 1, true);
        boolean hasEActive = hasAdjacent(level, pos, 5, true);

        if (letter == 26 && (hasEActive || hasAActive)) return currentState.setValue(ACTIVE, true);
        if (letter == 1 && hasZ) return currentState.setValue(ACTIVE, true);
        if (letter == 5 && (hasZ || hasL)) return currentState.setValue(ACTIVE, true);
        if (letter == 12 && hasE) return currentState.setValue(ACTIVE, true);

        return currentState.setValue(ACTIVE, false);
    }

    private boolean hasAdjacent(Level level, BlockPos pos, int targetLetter, boolean mustBeActive) {
        for (Direction dir : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (neighbor.getBlock() instanceof AltarBlock) {
                if (neighbor.getValue(LETTER) == targetLetter) {
                    if (!mustBeActive || neighbor.getValue(ACTIVE) || neighbor.getValue(GUESSED)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == NetherExp.ALTAR_BE.get() ? (lvl, p, st, be) -> AltarBlockEntity.tick(lvl, p, st, (AltarBlockEntity) be) : null;
    }
}