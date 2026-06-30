package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.item.AzazelScytheItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelScytheModel extends GeoModel<AzazelScytheItem> {
    @Override
    public ResourceLocation getModelResource(AzazelScytheItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/item/azazel_scythe.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelScytheItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/item/azazel_spear.png"); // Текстура общая!
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelScytheItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/item/azazel_scythe.animation.json");
    }
}