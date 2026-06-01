package com.benji.netherman.client.layer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.TraderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class TraderHintLayer extends GeoRenderLayer<TraderEntity> {
    private static final ResourceLocation HINT_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/trader_hint.png");
    private static final ResourceLocation WAIT_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/trader_wait.png");

    public TraderHintLayer(GeoEntityRenderer<TraderEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, TraderEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        int tradeState = animatable.getEntityData().get(TraderEntity.TRADE_STATE);
        boolean showHint = animatable.getEntityData().get(TraderEntity.SHOW_HINT);

        if (!showHint && tradeState != 1) return;

        ResourceLocation currentTexture = (tradeState == 1) ? WAIT_TEXTURE : HINT_TEXTURE;

        poseStack.pushPose();

        // 1. Поднимаем над головой
        poseStack.translate(0.0D, 3.0D, 0.0D);

        // 2. Поворачиваем к камере и инвертируем Y (как в Ghastly)
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // 3. Масштабируем (положительные значения, так как мы используем поворот на 180)
        poseStack.scale(0.03F, 0.03F, 0.03F);

        Matrix4f matrix4f = poseStack.last().pose();

        // Используем проверенный CutoutNoCull
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(currentTexture));

        float halfWidth = 14.0F;
        float height = 30.0F;
        int fullLight = 15728880;

        // 4. ОТРИСОВКА: Убираем matrix3f из нормалей и ставим жестко (0, 1, 0)
        // Теперь игра считает, что плоскость смотрит в небо, и отключает тени!

        // Левый верхний угол
        vertexconsumer.vertex(matrix4f, -halfWidth, 0, 0).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();

        // Левый нижний угол
        vertexconsumer.vertex(matrix4f, -halfWidth, height, 0).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();

        // Правый нижний угол
        vertexconsumer.vertex(matrix4f, halfWidth, height, 0).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();

        // Правый верхний угол
        vertexconsumer.vertex(matrix4f, halfWidth, 0, 0).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();

        poseStack.popPose();
    }
}