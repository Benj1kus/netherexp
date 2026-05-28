package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.ManipulatorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ManipulatorModel extends GeoModel<ManipulatorEntity> {
    @Override
    public ResourceLocation getModelResource(ManipulatorEntity animatable) {
        return switch (animatable.getHealthPhase()) {
            case 1 -> new ResourceLocation(NetherExp.MODID, "geo/manipulator_onehp.geo.json");
            case 2 -> new ResourceLocation(NetherExp.MODID, "geo/manipulator_zerohp.geo.json");
            default -> new ResourceLocation(NetherExp.MODID, "geo/manipulator.geo.json");
        };
    }

    @Override
    public ResourceLocation getTextureResource(ManipulatorEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/manipulator.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ManipulatorEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/manipulator.animation.json");
    }
}