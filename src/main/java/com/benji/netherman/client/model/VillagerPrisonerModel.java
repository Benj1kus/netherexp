package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.VillagerPrisonerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VillagerPrisonerModel extends GeoModel<VillagerPrisonerEntity> {
    @Override
    public ResourceLocation getModelResource(VillagerPrisonerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/villager_prisoner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VillagerPrisonerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/villager_prisoner.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VillagerPrisonerEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/villager_prisoner.animation.json");
    }
}