package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.EntranceBlockEntity;
import com.benji.netherman.block.entity.TraphiveBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TraphiveModel extends GeoModel<TraphiveBlockEntity> {
    @Override
    public ResourceLocation getModelResource(TraphiveBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/traphive.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TraphiveBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/traphive.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TraphiveBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/traphive.animation.json");
    }
}