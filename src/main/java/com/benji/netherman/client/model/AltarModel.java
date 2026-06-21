package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.AltarBlock;
import com.benji.netherman.block.entity.AltarBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.model.GeoModel;

public class AltarModel extends GeoModel<AltarBlockEntity> {

    private static final ResourceLocation DEFAULT_MODEL = new ResourceLocation(NetherExp.MODID, "geo/altar.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/altar.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/altar.animation.json");

    @Override
    public ResourceLocation getModelResource(AltarBlockEntity animatable) {
        BlockState state = animatable.getBlockState();

        if (state.hasProperty(AltarBlock.GUESSED) && state.getValue(AltarBlock.GUESSED)) {
            return DEFAULT_MODEL;
        }

        if (state.hasProperty(AltarBlock.LIT) && state.getValue(AltarBlock.LIT)) {
            int letterValue = state.getValue(AltarBlock.LETTER);

            if (letterValue > 0) {
                char letterChar = (char) ('a' + letterValue - 1);
                boolean isActive = state.getValue(AltarBlock.ACTIVE);

                if (isActive) {
                    return new ResourceLocation(NetherExp.MODID, "geo/altar_" + letterChar + "_active.geo.json");
                } else {
                    return new ResourceLocation(NetherExp.MODID, "geo/altar_" + letterChar + ".geo.json");
                }
            }
        }
        return DEFAULT_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AltarBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AltarBlockEntity animatable) {
        return ANIMATION;
    }
}