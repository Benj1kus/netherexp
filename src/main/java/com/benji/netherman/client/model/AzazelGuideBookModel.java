package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelGuideBookEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelGuideBookModel extends GeoModel<AzazelGuideBookEntity> {
    @Override
    public ResourceLocation getModelResource(AzazelGuideBookEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/azazel_guide_book.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelGuideBookEntity animatable) {
        // Если понадобится в будущем менять обложки страниц текстурой - можно привязать к BOOK_STATE
        return new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_guide_book_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelGuideBookEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/azazel_guide_book.animation.json");
    }
}