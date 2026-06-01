package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.StatueBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StatueModel extends GeoModel<StatueBlockEntity> {
    @Override
    public ResourceLocation getModelResource(StatueBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/statue.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StatueBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/statue.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StatueBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/statue.animation.json");
    }
}