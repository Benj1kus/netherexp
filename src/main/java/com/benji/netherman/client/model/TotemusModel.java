package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.TotemusBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TotemusModel extends GeoModel<TotemusBlockEntity> {
    @Override
    public ResourceLocation getModelResource(TotemusBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/totemus.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TotemusBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/totem_cave.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TotemusBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/totemus.animation.json");
    }
}