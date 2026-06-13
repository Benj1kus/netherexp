package com.benji.netherman.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GlowmaskLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private final ResourceLocation glowmaskTexture;

    public GlowmaskLayer(GeoRenderer<T> entityRendererIn, ResourceLocation glowmaskTexture) {
        super(entityRendererIn);
        this.glowmaskTexture = glowmaskTexture;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        RenderType glowRenderType = RenderType.eyes(this.glowmaskTexture);

        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType,
                bufferSource.getBuffer(glowRenderType), partialTick,
                15728880, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);
    }
}