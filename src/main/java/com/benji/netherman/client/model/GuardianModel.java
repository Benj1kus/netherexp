package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.GrandDoorBlockEntity;
import com.benji.netherman.entity.GuardianEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GuardianModel extends GeoModel<GuardianEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/guardian.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/guardian.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/guardian.animation.json");

    @Override
    public ResourceLocation getModelResource(GuardianEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GuardianEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GuardianEntity animatable) {
        return ANIMATION;
    }
}