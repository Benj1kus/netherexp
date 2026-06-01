package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.EyeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EyeModel extends GeoModel<EyeBlockEntity> {
    @Override
    public ResourceLocation getModelResource(EyeBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/eye_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EyeBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/eye_block.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EyeBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/eye_block.animation.json");
    }
}