package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidNetherMidBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.VoidNetherMidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VoidNetherMidRenderer extends GeoBlockRenderer<VoidNetherMidBlockEntity> {
    public VoidNetherMidRenderer(BlockEntityRendererProvider.Context context) {
        super(new VoidNetherMidModel());
        // Подключаем светящиеся элементы (глаза/цепи)
        addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(NetherExp.MODID, "textures/block/void_nether_emissive.png")));
    }

    // ВАЖНО: Разрешаем движку Майнкрафта обрабатывать альфа-канал (полупрозрачность)
    // Это позволит верхней части текстуры плавно растворяться в воздухе
    @Override
    public RenderType getRenderType(VoidNetherMidBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}