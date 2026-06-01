package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.BelieverEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BelieverModel extends GeoModel<BelieverEntity> {
    @Override
    public ResourceLocation getModelResource(BelieverEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/believer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BelieverEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/believer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BelieverEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/believer.animation.json");
    }
}