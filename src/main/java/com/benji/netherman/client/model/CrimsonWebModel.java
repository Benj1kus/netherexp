package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.CrimsonWebBlockEntity;
import com.benji.netherman.block.entity.EntranceBlockEntity;
import com.benji.netherman.entity.GhastlyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CrimsonWebModel extends GeoModel<CrimsonWebBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/crimson_web.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/crimson_web.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/crimson_web.animation.json");

    @Override
    public ResourceLocation getModelResource(CrimsonWebBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CrimsonWebBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CrimsonWebBlockEntity animatable) {
        return ANIMATION;
    }
}