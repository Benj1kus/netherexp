package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.FacePuzzleBlockEntity;
import com.benji.netherman.client.model.FacePuzzleLeftUpModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FacePuzzleLeftUpRenderer extends GeoBlockRenderer<FacePuzzleBlockEntity> {
    public FacePuzzleLeftUpRenderer(BlockEntityRendererProvider.Context context) {
        super(new FacePuzzleLeftUpModel());
    }
}