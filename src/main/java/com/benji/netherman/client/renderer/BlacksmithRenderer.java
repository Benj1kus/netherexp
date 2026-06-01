package com.benji.netherman.client.renderer;

import com.benji.netherman.client.layer.BlacksmithHintLayer;
import com.benji.netherman.client.model.BlacksmithModel;
import com.benji.netherman.entity.BlacksmithEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BlacksmithRenderer extends GeoEntityRenderer<BlacksmithEntity> {
    public BlacksmithRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BlacksmithModel());
        this.shadowRadius = 0.5f;

        // Подключаем слой всплывающей подсказки
        addRenderLayer(new BlacksmithHintLayer(this));
    }
}