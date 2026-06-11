package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.GrandDoorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GrandDoorModel extends GeoModel<GrandDoorBlockEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/grand_door.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/grand_door.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/grand_door.animation.json");

    @Override
    public ResourceLocation getModelResource(GrandDoorBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GrandDoorBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GrandDoorBlockEntity animatable) {
        return ANIMATION;
    }
}