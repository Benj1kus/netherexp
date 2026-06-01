package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.TraderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TraderModel extends GeoModel<TraderEntity> {
    @Override
    public ResourceLocation getModelResource(TraderEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/trader.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TraderEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/trader.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TraderEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/trader.animation.json");
    }
}