package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelEarthquakeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelEarthquakeModel extends GeoModel<AzazelEarthquakeEntity> {
    @Override
    public ResourceLocation getModelResource(AzazelEarthquakeEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/empty.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelEarthquakeEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/empty.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelEarthquakeEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/empty.animation.json");
    }
}