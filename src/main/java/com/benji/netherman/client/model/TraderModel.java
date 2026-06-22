package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.TraderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TraderModel extends GeoModel<TraderEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/trader.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/trader.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/trader.animation.json");

    @Override
    public ResourceLocation getModelResource(TraderEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TraderEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TraderEntity animatable) {
        return ANIMATION;
    }
}