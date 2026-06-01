package com.benji.netherman.client.renderer;

import com.benji.netherman.client.model.StatueBossunitModel;
import com.benji.netherman.entity.StatueBossunitEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StatueBossunitRenderer extends GeoEntityRenderer<StatueBossunitEntity> {
    public StatueBossunitRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new StatueBossunitModel());

        // Размер тени (если у обычной статуи 0.5f, тут тоже оставим так)
        this.shadowRadius = 0.5f;
    }
}