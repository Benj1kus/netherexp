package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.TraphiveBlockEntity;
import com.benji.netherman.entity.TraderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TraphiveModel extends GeoModel<TraphiveBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/traphive.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/traphive.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/traphive.animation.json");

    @Override
    public ResourceLocation getModelResource(TraphiveBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TraphiveBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TraphiveBlockEntity animatable) {
        return ANIMATION;
    }
}