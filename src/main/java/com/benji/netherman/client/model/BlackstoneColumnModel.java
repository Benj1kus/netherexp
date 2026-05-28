package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.BlackstoneColumnBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackstoneColumnModel extends GeoModel<BlackstoneColumnBlockEntity> {
    @Override
    public ResourceLocation getModelResource(BlackstoneColumnBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/blackstone_column.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackstoneColumnBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_column.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlackstoneColumnBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/blackstone_column.animation.json");
    }
}