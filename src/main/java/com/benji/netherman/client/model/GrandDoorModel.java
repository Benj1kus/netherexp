package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.GrandDoorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GrandDoorModel extends GeoModel<GrandDoorBlockEntity> {
    @Override
    public ResourceLocation getModelResource(GrandDoorBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/grand_door.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GrandDoorBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/grand_door.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GrandDoorBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/grand_door.animation.json");
    }
}