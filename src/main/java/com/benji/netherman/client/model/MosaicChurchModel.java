package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.MosaicChurchBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MosaicChurchModel extends GeoModel<MosaicChurchBlockEntity> {
    @Override
    public ResourceLocation getModelResource(MosaicChurchBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/mosaic_church.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MosaicChurchBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/mosaic_church.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MosaicChurchBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/mosaic_church.animation.json");
    }
}