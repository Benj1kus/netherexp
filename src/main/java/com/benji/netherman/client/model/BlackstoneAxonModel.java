package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.BlackstoneAxonBlockEntity;
import com.benji.netherman.block.entity.CrimsonWebBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackstoneAxonModel extends GeoModel<BlackstoneAxonBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/blackstone_axon.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_axon.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/blackstone_axon.animation.json");

    @Override
    public ResourceLocation getModelResource(BlackstoneAxonBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BlackstoneAxonBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BlackstoneAxonBlockEntity animatable) {
        return ANIMATION;
    }
}