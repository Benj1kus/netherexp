package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelSpikeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelSpikeModel extends GeoModel<AzazelSpikeEntity> {
    @Override
    public ResourceLocation getModelResource(AzazelSpikeEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/azazel_spikes.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelSpikeEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_spikes.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelSpikeEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/azazel_spikes.animation.json");
    }
}