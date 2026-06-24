package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.MazeDoorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MazeDoorModel extends GeoModel<MazeDoorBlockEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(NetherExp.MODID, "geo/maze_door.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/maze_door.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NetherExp.MODID, "animations/maze_door.animation.json");

    @Override
    public ResourceLocation getModelResource(MazeDoorBlockEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MazeDoorBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MazeDoorBlockEntity animatable) {
        return ANIMATION;
    }
}