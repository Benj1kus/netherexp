package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.StatueBossunitEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StatueBossunitModel extends GeoModel<StatueBossunitEntity> {
    @Override
    public ResourceLocation getModelResource(StatueBossunitEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/statue_bossunit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StatueBossunitEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/statue_bossunit.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StatueBossunitEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/statue_bossunit.animation.json");
    }
}