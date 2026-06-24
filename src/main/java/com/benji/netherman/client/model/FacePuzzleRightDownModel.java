package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.FacePuzzleBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FacePuzzleRightDownModel extends GeoModel<FacePuzzleBlockEntity> {
    @Override
    public ResourceLocation getModelResource(FacePuzzleBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/face_puzzle_right_down.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FacePuzzleBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/block/face_puzzle.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FacePuzzleBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/right_down.animation.json");
    }
}