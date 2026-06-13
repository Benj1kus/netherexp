package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.StatueStandBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.StatueStandModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class StatueStandRenderer extends GeoBlockRenderer<StatueStandBlockEntity> {
    public StatueStandRenderer(BlockEntityRendererProvider.Context context) {
        super(new StatueStandModel());
    }
}