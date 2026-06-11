package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.GrandDoorBlockEntity;
import com.benji.netherman.entity.GhastlyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GhastlyModel extends GeoModel<GhastlyEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/ghastly.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/ghastly.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/ghastly.animation.json");

    @Override
    public ResourceLocation getModelResource(GhastlyEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GhastlyEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GhastlyEntity animatable) {
        return ANIMATION;
    }
}