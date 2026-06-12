package com.benji.netherman.client.renderer;

import com.benji.netherman.client.layer.TraderHintLayer;
import com.benji.netherman.client.model.TraderModel;
import com.benji.netherman.entity.TraderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TraderRenderer extends GeoEntityRenderer<TraderEntity> {
    public TraderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TraderModel());
        this.shadowRadius = 0.5f;
        addRenderLayer(new TraderHintLayer(this));
    }
}