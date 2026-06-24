package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.TotemusPuzzleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TotemusPuzzleModel extends GeoModel<TotemusPuzzleEntity> {

    @Override
    public ResourceLocation getModelResource(TotemusPuzzleEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/totemus_puzzle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TotemusPuzzleEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/totemus_puzzle_red.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TotemusPuzzleEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/totemus_puzzle.animation.json");
    }
}