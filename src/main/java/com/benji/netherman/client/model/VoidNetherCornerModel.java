package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidNetherCornerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidNetherCornerModel extends GeoModel<VoidNetherCornerBlockEntity> {
    @Override
    public ResourceLocation getModelResource(VoidNetherCornerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/voidnether_corner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidNetherCornerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/void_nether.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidNetherCornerBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/voidnether_corner.animation.json");
    }
}