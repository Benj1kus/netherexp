package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidCornerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VoidCornerModel extends GeoModel<VoidCornerBlockEntity> {
    @Override
    public ResourceLocation getModelResource(VoidCornerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/void_corner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VoidCornerBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/void.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VoidCornerBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/void_corner.animation.json");
    }
}