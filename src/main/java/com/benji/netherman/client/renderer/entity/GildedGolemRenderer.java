package com.benji.netherman.client.renderer.entity;

import com.benji.netherman.client.model.GildedGolemModel;
import com.benji.netherman.entity.GildedGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GildedGolemRenderer extends GeoEntityRenderer<GildedGolemEntity> {
    public GildedGolemRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GildedGolemModel());

        this.shadowRadius = 0.7f;
    }
}