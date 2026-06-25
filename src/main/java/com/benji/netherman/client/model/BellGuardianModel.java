package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.BellGuardianEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BellGuardianModel extends GeoModel<BellGuardianEntity> {

    @Override
    public ResourceLocation getModelResource(BellGuardianEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/bell_guardian.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BellGuardianEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/bell_guardian.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BellGuardianEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/bell_guardian.animation.json");
    }
}