package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.WelcomerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WelcomerModel extends GeoModel<WelcomerEntity> {
    @Override
    public ResourceLocation getModelResource(WelcomerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/welcomer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WelcomerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/welcomer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WelcomerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/welcomer.animation.json");
    }
}