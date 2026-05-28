package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidNetherMidCornerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidNetherMidCornerModel extends GeoModel<VoidNetherMidCornerBlockEntity> {
    @Override
    public ResourceLocation getModelResource(VoidNetherMidCornerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/voidnether_midcorner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidNetherMidCornerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/void_nether.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidNetherMidCornerBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/voidnether_midcorner.animation.json");
    }
}