package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.FacePuzzleBlockEntity;
import com.benji.netherman.client.model.FacePuzzleLeftUpModel;
import com.benji.netherman.client.model.FacePuzzleRightUpModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FacePuzzleRightUpRenderer extends GeoBlockRenderer<FacePuzzleBlockEntity> {
    public FacePuzzleRightUpRenderer(BlockEntityRendererProvider.Context context) {
        super(new FacePuzzleRightUpModel());
    }
}