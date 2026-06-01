package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.StatueBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.StatueModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class StatueRenderer extends GeoBlockRenderer<StatueBlockEntity> {
    public StatueRenderer(BlockEntityRendererProvider.Context context) {
        super(new StatueModel());

        // Указываем путь к твоему светящемуся слою
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_column_emissive.png");

        // Добавляем слой из твоего старого проекта
        addRenderLayer(new GenericEmissiveLayer<>(this, emissiveTexture));
    }
}