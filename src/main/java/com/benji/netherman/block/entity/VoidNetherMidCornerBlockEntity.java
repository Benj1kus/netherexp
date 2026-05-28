package com.benji.netherman.block.entity;

import com.benji.netherman.NetherExp;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VoidNetherMidCornerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public VoidNetherMidCornerBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.VOIDMIDCORNERNETHER_BE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}