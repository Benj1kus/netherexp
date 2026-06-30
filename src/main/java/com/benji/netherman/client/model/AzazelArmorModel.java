package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.item.AzazelArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelArmorModel extends GeoModel<AzazelArmorItem> {
    @Override
    public ResourceLocation getModelResource(AzazelArmorItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/azazel_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelArmorItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/models/armor/believer_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelArmorItem animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/azazel_armor.animation.json");
    }
}