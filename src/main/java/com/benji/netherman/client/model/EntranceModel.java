package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.EntranceBlockEntity;
import com.benji.netherman.entity.GhastlyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EntranceModel extends GeoModel<EntranceBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/entrance.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/entrance.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/entrance.animation.json");

    @Override
    public ResourceLocation getModelResource(EntranceBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(EntranceBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(EntranceBlockEntity animatable) {
        return ANIMATION;
    }
}