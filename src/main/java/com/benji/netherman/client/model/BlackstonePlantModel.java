package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.BlackstonePlantBlockEntity;
import com.benji.netherman.block.entity.CrimsonWebBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackstonePlantModel extends GeoModel<BlackstonePlantBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/blackstone_plant.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_plant.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/blackstone_plant.animation.json");

    @Override
    public ResourceLocation getModelResource(BlackstonePlantBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BlackstonePlantBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BlackstonePlantBlockEntity animatable) {
        return ANIMATION;
    }
}