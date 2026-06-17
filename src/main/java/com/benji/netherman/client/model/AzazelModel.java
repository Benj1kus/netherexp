package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelModel extends GeoModel<AzazelEntity> {

    private static final ResourceLocation MODEL_NORMAL = new ResourceLocation(NetherExp.MODID, "geo/azazel.geo.json");
    private static final ResourceLocation MODEL_LOW_HP = new ResourceLocation(NetherExp.MODID, "geo/azazel_lowhp.geo.json");
    private static final ResourceLocation TEX_GOLD = new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_gold.png");

    private static final ResourceLocation TEX_PRAY = new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_pray.png");
    private static final ResourceLocation TEX_NORMAL = new ResourceLocation(NetherExp.MODID, "textures/entity/azazel.png");
    private static final ResourceLocation TEX_DAMAGED = new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_damaged.png");

    @Override
    public ResourceLocation getModelResource(AzazelEntity animatable) {
        if (animatable.getEntityData().get(AzazelEntity.PHASE_STATE) == 2) {
            return MODEL_LOW_HP;
        }
        return MODEL_NORMAL;
    }

    @Override
    public ResourceLocation getTextureResource(AzazelEntity animatable) {
        if (animatable.getEntityData().get(AzazelEntity.ATTACK_STATE) == 13) {
            return TEX_GOLD;
        }

        if (!animatable.getEntityData().get(AzazelEntity.IS_AGGRO)) {
            return TEX_PRAY;
        }

        int phase = animatable.getEntityData().get(AzazelEntity.PHASE_STATE);
        if (phase >= 1) {
            return TEX_DAMAGED;
        }

        return TEX_NORMAL;
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/azazel.animation.json");
    }
}