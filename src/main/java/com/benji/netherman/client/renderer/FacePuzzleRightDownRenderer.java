package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.FacePuzzleBlockEntity;
import com.benji.netherman.client.model.FacePuzzleRightDownModel;
import com.benji.netherman.client.model.FacePuzzleRightUpModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FacePuzzleRightDownRenderer extends GeoBlockRenderer<FacePuzzleBlockEntity> {
    public FacePuzzleRightDownRenderer(BlockEntityRendererProvider.Context context) {
        super(new FacePuzzleRightDownModel());
    }
}