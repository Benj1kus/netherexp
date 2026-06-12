package com.benji.netherman.client.renderer.entity;

import com.benji.netherman.client.model.WelcomerModel;
import com.benji.netherman.entity.WelcomerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WelcomerRenderer extends GeoEntityRenderer<WelcomerEntity> {
    public WelcomerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WelcomerModel());
        this.shadowRadius = 0.5f;
    }
}