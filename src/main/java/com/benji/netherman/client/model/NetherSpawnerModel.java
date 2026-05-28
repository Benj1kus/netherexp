package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.NetherSpawnerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NetherSpawnerModel extends GeoModel<NetherSpawnerBlockEntity> {
    @Override
    public ResourceLocation getModelResource(NetherSpawnerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/nether_spawner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NetherSpawnerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/nether_spawner.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NetherSpawnerBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/nether_spawner.animation.json");
    }
}