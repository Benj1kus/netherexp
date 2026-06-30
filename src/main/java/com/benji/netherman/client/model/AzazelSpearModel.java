package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.item.AzazelSpearItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelSpearModel extends GeoModel<AzazelSpearItem> {
    @Override
    public ResourceLocation getModelResource(AzazelSpearItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/item/azazel_spear.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelSpearItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/item/azazel_spear.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelSpearItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/item/azazel_spear.animation.json");
    }
}