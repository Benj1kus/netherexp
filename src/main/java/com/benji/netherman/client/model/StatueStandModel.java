package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.StatueStandBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StatueStandModel extends GeoModel<StatueStandBlockEntity> {
    @Override
    public ResourceLocation getModelResource(StatueStandBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/statue_stand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StatueStandBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/statue_stand.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StatueStandBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/statue_stand.animation.json");
    }
}