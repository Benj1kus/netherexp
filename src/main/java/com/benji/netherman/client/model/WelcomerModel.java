package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidNetherMidBlockEntity;
import com.benji.netherman.entity.WelcomerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WelcomerModel extends GeoModel<WelcomerEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/welcomer.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/welcomer.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/welcomer.animation.json");

    @Override
    public ResourceLocation getModelResource(WelcomerEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(WelcomerEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WelcomerEntity animatable) {
        return ANIMATION;
    }
}