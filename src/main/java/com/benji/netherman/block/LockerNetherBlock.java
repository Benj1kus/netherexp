package com.benji.netherman.block;

import com.benji.netherman.NetherExp;
import com.benji.netherman.network.ModMessages;
import com.benji.netherman.network.TotemAnimationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashSet;
import java.util.Set;

public class LockerNetherBlock extends HorizontalDirectionalBlock {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public LockerNetherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            
            CompoundTag playerData = player.getPersistentData();
            int brokenCount = playerData.getInt("LockerQuestProgress") + 1;

            Item animationItem = NetherExp.QUEST_ICON_1.get();
            if (brokenCount == 2) animationItem = NetherExp.QUEST_ICON_2.get();
            if (brokenCount >= 3) animationItem = NetherExp.QUEST_ICON_3.get();

            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.MASTER, 2.0F, 0.5F);

            if (player instanceof ServerPlayer serverPlayer) {
                ModMessages.sendToPlayer(new TotemAnimationPacket(new ItemStack(animationItem)), serverPlayer);
            }

            if (brokenCount >= 3) {
                player.getInventory().add(new ItemStack(NetherExp.CRIMSON_HONEY_BOTTLE.get()));
                playerData.putInt("LockerQuestProgress", 0);
            } else {
                playerData.putInt("LockerQuestProgress", brokenCount);
            }

            triggerChainReactionAndPillars((ServerLevel) level, pos);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private void triggerChainReactionAndPillars(ServerLevel level, BlockPos startPos) {
        Set<BlockPos> lockersToDestroy = new HashSet<>();

        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos checkPos = startPos.offset(x, y, z);
                    if (level.getBlockState(checkPos).is(this)) {
                        lockersToDestroy.add(checkPos);
                    }
                }
            }
        }

        for (BlockPos lockerPos : lockersToDestroy) {
            level.setBlock(lockerPos, NetherExp.BLACKSTONE_COLUMN.get().defaultBlockState(), 3);
            level.scheduleTick(lockerPos, NetherExp.BLACKSTONE_COLUMN.get(), 2);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}