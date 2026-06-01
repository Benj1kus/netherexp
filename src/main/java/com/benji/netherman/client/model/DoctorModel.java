package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.DoctorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DoctorModel extends GeoModel<DoctorEntity> {
    @Override
    public ResourceLocation getModelResource(DoctorEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/doctor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DoctorEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/doctor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DoctorEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/doctor.animation.json");
    }
}