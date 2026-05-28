package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidMidBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidMidModel extends GeoModel<VoidMidBlockEntity> {
    @Override
    public ResourceLocation getModelResource(VoidMidBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/void_mid.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidMidBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/void.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidMidBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/void_mid.animation.json");
    }
}