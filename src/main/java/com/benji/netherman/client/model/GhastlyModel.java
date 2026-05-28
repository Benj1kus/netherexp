package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.GhastlyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GhastlyModel extends GeoModel<GhastlyEntity> {
    @Override
    public ResourceLocation getModelResource(GhastlyEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/ghastly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GhastlyEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/ghastly.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GhastlyEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/ghastly.animation.json");
    }
}