package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.item.AzazelShieldItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelShieldModel extends GeoModel<AzazelShieldItem> {
    @Override
    public ResourceLocation getModelResource(AzazelShieldItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/item/azazel_shield.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelShieldItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/item/azazel_shield.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelShieldItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/item/azazel_shield.animation.json");
    }
}