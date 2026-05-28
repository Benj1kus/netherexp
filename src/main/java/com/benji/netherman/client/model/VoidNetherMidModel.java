package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidNetherMidBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidNetherMidModel extends GeoModel<VoidNetherMidBlockEntity> {
    @Override
    public ResourceLocation getModelResource(VoidNetherMidBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/voidnether_mid.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidNetherMidBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/void_nether.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidNetherMidBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/voidnether_mid.animation.json");
    }
}