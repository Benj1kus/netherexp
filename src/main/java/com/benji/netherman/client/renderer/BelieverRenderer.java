package com.benji.netherman.client.renderer;

import com.benji.netherman.client.layer.BelieverPrayEmissiveLayer;
import com.benji.netherman.client.model.BelieverModel;
import com.benji.netherman.entity.BelieverEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BelieverRenderer extends GeoEntityRenderer<BelieverEntity> {
    public BelieverRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BelieverModel());
        this.shadowRadius = 0.5f;

        // Подключаем условный светящийся слой
        addRenderLayer(new BelieverPrayEmissiveLayer(this));
    }
}