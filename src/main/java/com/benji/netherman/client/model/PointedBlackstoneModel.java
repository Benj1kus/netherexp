package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.PointedBlackstoneBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import software.bernie.geckolib.model.GeoModel;

public class PointedBlackstoneModel extends GeoModel<PointedBlackstoneBlockEntity> {
    @Override
    public ResourceLocation getModelResource(PointedBlackstoneBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/pointed_blackstone.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PointedBlackstoneBlockEntity animatable) {
        BlockState state = animatable.getBlockState();

        
        if (state.hasProperty(PointedDripstoneBlock.TIP_DIRECTION) && state.hasProperty(PointedDripstoneBlock.THICKNESS)) {
            Direction dir = state.getValue(PointedDripstoneBlock.TIP_DIRECTION);
            DripstoneThickness thickness = state.getValue(PointedDripstoneBlock.THICKNESS);

            
            String dirStr = dir == Direction.DOWN ? "down" : "up";

            
            String thickStr = thickness.getSerializedName();

            
            if (thickStr.equals("merge")) {
                thickStr = "tip_merge";
            }

            
            return new ResourceLocation(NetherExp.MODID, "textures/block/pointed_blackstone_" + dirStr + "_" + thickStr + ".png");
        }

        return new ResourceLocation(NetherExp.MODID, "textures/block/pointed_blackstone_down_base.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PointedBlackstoneBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/empty.animation.json");
    }
}