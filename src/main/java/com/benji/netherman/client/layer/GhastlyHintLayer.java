package com.benji.netherman.client.layer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.GhastlyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft; // ДОБАВЛЕН ИМПОРТ
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GhastlyHintLayer extends GeoRenderLayer<GhastlyEntity> {
    private static final ResourceLocation HINT_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/ghastly_hint_sheet.png");

    public GhastlyHintLayer(GeoRenderer<GhastlyEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, GhastlyEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!animatable.getEntityData().get(GhastlyEntity.SHOW_HINT)) return;
        int totalFrames = 8;
        int frame = (animatable.tickCount / 3) % totalFrames;
        float vHeight = 1.0f / totalFrames;
        float minV = frame * vHeight;
        float maxV = minV + vHeight;

        poseStack.pushPose();
        poseStack.translate(0, 1.5, 0);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(HINT_TEXTURE));

        float halfWidth = 0.5f;
        float halfHeight = 0.5f;
        vertexConsumer.vertex(matrix, -halfWidth, -halfHeight, 0.0F).color(255, 255, 255, 255).uv(1.0F, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix, halfWidth, -halfHeight, 0.0F).color(255, 255, 255, 255).uv(0.0F, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix, halfWidth, halfHeight, 0.0F).color(255, 255, 255, 255).uv(0.0F, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix, -halfWidth, halfHeight, 0.0F).color(255, 255, 255, 255).uv(1.0F, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0.0F, 1.0F, 0.0F).endVertex();

        poseStack.popPose();
    }
}