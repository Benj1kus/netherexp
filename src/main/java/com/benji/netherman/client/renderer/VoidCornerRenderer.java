package com.benji.netherman.client.renderer;

import com.benji.netherman.block.entity.VoidCornerBlockEntity;
import com.benji.netherman.client.model.VoidCornerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VoidCornerRenderer extends GeoBlockRenderer<VoidCornerBlockEntity> {
    public VoidCornerRenderer(BlockEntityRendererProvider.Context context) {
        super(new VoidCornerModel());
        // Emissive слой удален, так как он не нужен
    }

    // ВАЖНО: Разрешаем движку Майнкрафта обрабатывать альфа-канал (полупрозрачность)
    // Это позволит верхней части текстуры плавно растворяться в воздухе
    @Override
    public RenderType getRenderType(VoidCornerBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}