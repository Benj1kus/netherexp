package com.benji.netherman.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GenericEmissiveLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private final ResourceLocation emissiveTexture;

    public GenericEmissiveLayer(GeoRenderer<T> entityRendererIn, ResourceLocation emissiveTexture) {
        super(entityRendererIn);
        this.emissiveTexture = emissiveTexture;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {

        // RenderType.eyes() заставляет текстуру игнорировать уровень освещения в игре
        RenderType glowRenderType = RenderType.eyes(this.emissiveTexture);
        VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);

        // Рендерим слой с максимальным значением света (15728880)
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowRenderType,
                glowBuffer, partialTick, 15728880, packedOverlay, 0.5F, 0.5F, 0.5F, 0.5F);
    }
}