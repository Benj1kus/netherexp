package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelHumanEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelHumanModel extends GeoModel<AzazelHumanEntity> {
    @Override
    public ResourceLocation getModelResource(AzazelHumanEntity animatable) {
        if (animatable.getEntityData().get(AzazelHumanEntity.IS_PHASE_2)) {
            return new ResourceLocation(NetherExp.MODID, "geo/azazel_human_lowhp.geo.json");
        }
        return new ResourceLocation(NetherExp.MODID, "geo/azazel_human.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelHumanEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_human.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelHumanEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/azazel_human.animation.json");
    }
}