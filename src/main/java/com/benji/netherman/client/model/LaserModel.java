package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.LaserEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LaserModel extends GeoModel<LaserEntity> {
    @Override
    public ResourceLocation getModelResource(LaserEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/laser.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LaserEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/laser.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LaserEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/laser.animation.json");
    }
}