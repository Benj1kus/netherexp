package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.EntranceBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EntranceModel extends GeoModel<EntranceBlockEntity> {
    @Override
    public ResourceLocation getModelResource(EntranceBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/entrance.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntranceBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/entrance.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EntranceBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/entrance.animation.json");
    }
}