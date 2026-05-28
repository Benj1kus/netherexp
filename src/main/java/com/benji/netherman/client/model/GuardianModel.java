package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.GuardianEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GuardianModel extends GeoModel<GuardianEntity> {
    @Override
    public ResourceLocation getModelResource(GuardianEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/guardian.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GuardianEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/guardian.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GuardianEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/guardian.animation.json");
    }
}