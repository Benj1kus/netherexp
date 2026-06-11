package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.EyeBlockEntity;
import com.benji.netherman.entity.GhastlyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EyeModel extends GeoModel<EyeBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/eye_block.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/eye_block.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/eye_block.animation.json");

    @Override
    public ResourceLocation getModelResource(EyeBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(EyeBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(EyeBlockEntity animatable) {
        return ANIMATION;
    }
}