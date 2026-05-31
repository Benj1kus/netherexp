package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.MosaicChurchBlockEntity;
import com.benji.netherman.client.layer.MosaicChurchAnimatedEmissiveLayer;
import com.benji.netherman.client.model.MosaicChurchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class MosaicChurchRenderer extends GeoBlockRenderer<MosaicChurchBlockEntity> {
    public MosaicChurchRenderer(BlockEntityRendererProvider.Context context) {
        super(new MosaicChurchModel());

        // ПОДКЛЮЧАЕМ НАШ АНИМИРОВАННЫЙ СЛОЙ ВМЕСТО СТАТИЧНОГО
        addRenderLayer(new MosaicChurchAnimatedEmissiveLayer(this));
    }

    @Override
    public RenderType getRenderType(MosaicChurchBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}