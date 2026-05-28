package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidMidCornerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidMidCornerModel extends GeoModel<VoidMidCornerBlockEntity> {
    @Override
    public ResourceLocation getModelResource(VoidMidCornerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/void_midcorner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidMidCornerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/void.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidMidCornerBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/void_midcorner.animation.json");
    }
}