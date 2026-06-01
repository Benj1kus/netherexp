package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.StatueEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StatueModel extends GeoModel<StatueEntity> {
    @Override
    public ResourceLocation getModelResource(StatueEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/statue_entity.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StatueEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/statue_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StatueEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/statue_entity.animation.json");
    }
}