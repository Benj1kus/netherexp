package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.CrimsonWebBlockEntity;
import com.benji.netherman.block.entity.EntranceBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CrimsonWebModel extends GeoModel<CrimsonWebBlockEntity> {
    @Override
    public ResourceLocation getModelResource(CrimsonWebBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/crimson_web.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CrimsonWebBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/crimson_web.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CrimsonWebBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/crimson_web.animation.json");
    }
}