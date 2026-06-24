package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.MazeDoorBlockEntity;
import com.benji.netherman.client.model.MazeDoorModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class MazeDoorRenderer extends GeoBlockRenderer<MazeDoorBlockEntity> {

    public MazeDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new MazeDoorModel());
    }
    @Override
    public ResourceLocation getTextureLocation(MazeDoorBlockEntity animatable) {
        if (animatable.requiresKey) {
            return new ResourceLocation(NetherExp.MODID, "textures/block/maze_door_key.png");
        }
        return new ResourceLocation(NetherExp.MODID, "textures/block/maze_door.png");
    }
}