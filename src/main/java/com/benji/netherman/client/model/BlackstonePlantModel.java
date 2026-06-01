package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.BlackstonePlantBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackstonePlantModel extends GeoModel<BlackstonePlantBlockEntity> {
    @Override
    public ResourceLocation getModelResource(BlackstonePlantBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/blackstone_plant.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackstonePlantBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_plant.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlackstonePlantBlockEntity animatable) {
        // Если пока нет анимаций, можно возвращать null или пустышку
        return new ResourceLocation(NetherExp.MODID, "animations/blackstone_plant.animation.json");
    }
}