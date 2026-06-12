package com.benji.netherman.client.layer;

import com.benji.netherman.entity.GuardianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GuardianEmissiveLayer extends GeoRenderLayer<GuardianEntity> {
    private final ResourceLocation emissiveTexture;

    public GuardianEmissiveLayer(GeoRenderer<GuardianEntity> entityRendererIn, ResourceLocation emissiveTexture) {
        super(entityRendererIn);
        this.emissiveTexture = emissiveTexture;
    }

    @Override
    public void render(PoseStack poseStack, GuardianEntity animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (!animatable.isBuffed()) return;
        RenderType glowRenderType = RenderType.entityTranslucentEmissive(this.emissiveTexture);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType,
                glowBuffer, partialTick, 15728880, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}