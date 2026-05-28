package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.BlackstoneAxonBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackstoneAxonModel extends GeoModel<BlackstoneAxonBlockEntity> {
    @Override
    public ResourceLocation getModelResource(BlackstoneAxonBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/blackstone_axon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackstoneAxonBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_axon.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlackstoneAxonBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/blackstone_axon.animation.json");
    }
}