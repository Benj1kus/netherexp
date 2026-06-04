package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.GildedGolemEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GildedGolemModel extends GeoModel<GildedGolemEntity> {

    // Пути к файлам модели и анимации
    private static final ResourceLocation MODEL_RESOURCE = new ResourceLocation(NetherExp.MODID, "geo/gilded_golem.geo.json");
    private static final ResourceLocation ANIMATION_RESOURCE = new ResourceLocation(NetherExp.MODID, "animations/gilded_golem.animation.json");

    // Пути к текстурам
    private static final ResourceLocation TEX_NORMAL = new ResourceLocation(NetherExp.MODID, "textures/entity/gilded_golem.png");
    private static final ResourceLocation TEX_DAMAGED = new ResourceLocation(NetherExp.MODID, "textures/entity/gilded_golem_damaged.png");
    private static final ResourceLocation TEX_HEAL = new ResourceLocation(NetherExp.MODID, "textures/entity/gilded_golem_damaged_heal.png");

    @Override
    public ResourceLocation getModelResource(GildedGolemEntity object) {
        return MODEL_RESOURCE;
    }

    @Override
    public ResourceLocation getTextureResource(GildedGolemEntity object) {
        // Считываем состояние здоровья (0 = цел, 1 = ранен, 2 = лечится)
        int state = object.getEntityData().get(GildedGolemEntity.TEXTURE_STATE);

        if (state == 2) {
            return TEX_HEAL;
        } else if (state == 1) {
            return TEX_DAMAGED;
        }

        return TEX_NORMAL;
    }

    @Override
    public ResourceLocation getAnimationResource(GildedGolemEntity animatable) {
        return ANIMATION_RESOURCE;
    }
}