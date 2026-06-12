package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.TraphiveBlockEntity;
import com.benji.netherman.client.layer.TraphiveAnimatedEmissiveLayer; // Наш новый слой
import com.benji.netherman.client.model.TraphiveModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TraphiveRenderer extends GeoBlockRenderer<TraphiveBlockEntity> {
    public TraphiveRenderer(BlockEntityRendererProvider.Context context) {
        super(new TraphiveModel());
        addRenderLayer(new TraphiveAnimatedEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(TraphiveBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}