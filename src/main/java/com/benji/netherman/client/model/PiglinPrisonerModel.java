package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.PiglinPrisonerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PiglinPrisonerModel extends GeoModel<PiglinPrisonerEntity> {
    @Override
    public ResourceLocation getModelResource(PiglinPrisonerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/piglin_prisoner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PiglinPrisonerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/piglin_prisoner.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PiglinPrisonerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/piglin_prisoner.animation.json");
    }
}