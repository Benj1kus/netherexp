package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelSplashEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelSplashModel extends GeoModel<AzazelSplashEntity> {
    @Override
    public ResourceLocation getModelResource(AzazelSplashEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/splash.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelSplashEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/splash.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelSplashEntity animatable) {
        
        return new ResourceLocation(NetherExp.MODID, "animations/empty.animation.json");
    }
}