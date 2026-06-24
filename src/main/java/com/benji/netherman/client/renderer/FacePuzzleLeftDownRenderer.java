package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.FacePuzzleBlockEntity;
import com.benji.netherman.client.model.FacePuzzleLeftDownModel;
import com.benji.netherman.client.model.FacePuzzleLeftUpModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FacePuzzleLeftDownRenderer extends GeoBlockRenderer<FacePuzzleBlockEntity> {
    public FacePuzzleLeftDownRenderer(BlockEntityRendererProvider.Context context) {
        super(new FacePuzzleLeftDownModel());
    }
}