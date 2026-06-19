package com.benji.netherman.item;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelGuideBookEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class AzazelGuideBookItem extends Item {
    public AzazelGuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());

            AzazelGuideBookEntity book = NetherExp.AZAZEL_GUIDE_BOOK_ENTITY.get().create(level);
            if (book != null) {
                book.moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D,
                        context.getPlayer() != null ? context.getPlayer().getYRot() : 0.0F, 0.0F);

                level.addFreshEntity(book);

                level.playSound(null, targetPos, SoundEvents.CHEST_OPEN, SoundSource.NEUTRAL, 0.8F, 0.7F);

                if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                    context.getItemInHand().shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}