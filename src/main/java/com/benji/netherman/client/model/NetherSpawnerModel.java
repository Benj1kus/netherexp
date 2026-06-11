package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.GrandDoorBlockEntity;
import com.benji.netherman.block.entity.NetherSpawnerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NetherSpawnerModel extends GeoModel<NetherSpawnerBlockEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/nether_spawner.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/nether_spawner.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/nether_spawner.animation.json");

    @Override
    public ResourceLocation getModelResource(NetherSpawnerBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(NetherSpawnerBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(NetherSpawnerBlockEntity animatable) {
        return ANIMATION;
    }
}