package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.VoidMidBlockEntity;
import com.benji.netherman.client.model.VoidMidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VoidMidRenderer extends GeoBlockRenderer<VoidMidBlockEntity> {
    public VoidMidRenderer(BlockEntityRendererProvider.Context context) {
        super(new VoidMidModel());
        // Emissive слой удален, так как он не нужен
    }


    // ВАЖНО: Разрешаем движку Майнкрафта обрабатывать альфа-канал (полупрозрачность)
    // Это позволит верхней части текстуры плавно растворяться в воздухе
    @Override
    public RenderType getRenderType(VoidMidBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}