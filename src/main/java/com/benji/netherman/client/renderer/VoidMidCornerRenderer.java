package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.VoidMidCornerBlockEntity;
import com.benji.netherman.client.model.VoidMidCornerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VoidMidCornerRenderer extends GeoBlockRenderer<VoidMidCornerBlockEntity> {
    public VoidMidCornerRenderer(BlockEntityRendererProvider.Context context) {
        super(new VoidMidCornerModel());
        // Emissive слой удален, так как он не нужен
    }

    // ВАЖНО: Разрешаем движку Майнкрафта обрабатывать альфа-канал (полупрозрачность)
    // Это позволит верхней части текстуры плавно растворяться в воздухе
    @Override
    public RenderType getRenderType(VoidMidCornerBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}