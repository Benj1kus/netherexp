package com.benji.netherman.client.renderer.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.layer.GhastlyHintLayer;
import com.benji.netherman.client.model.GhastlyModel;
import com.benji.netherman.entity.GhastlyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GhastlyRenderer extends GeoEntityRenderer<GhastlyEntity> {
    public GhastlyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GhastlyModel());
        this.shadowRadius = 0.3f;

        addRenderLayer(new GhastlyHintLayer(this));
    }
}