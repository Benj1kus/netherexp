package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.StatueStandBlockEntity;
import com.benji.netherman.entity.StatueEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StatueStandModel extends GeoModel<StatueStandBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/statue_stand.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/statue_stand.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/statue_stand.animation.json");

    @Override
    public ResourceLocation getModelResource(StatueStandBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(StatueStandBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(StatueStandBlockEntity animatable) {
        return ANIMATION;
    }
}