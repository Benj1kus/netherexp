package com.benji.netherman.client.layer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.DoctorEntity;
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

public class DoctorHintLayer extends GeoRenderLayer<DoctorEntity> {

    // 3 разные текстуры подсказок
    private static final ResourceLocation HINT_1 = new ResourceLocation(NetherExp.MODID, "textures/entity/doctor_hint.png");
    private static final ResourceLocation HINT_2 = new ResourceLocation(NetherExp.MODID, "textures/entity/doctoradditional_hint.png");
    private static final ResourceLocation HINT_3 = new ResourceLocation(NetherExp.MODID, "textures/entity/doctortrade_hint.png");

    public DoctorHintLayer(GeoEntityRenderer<DoctorEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, DoctorEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        int hintState = animatable.getEntityData().get(DoctorEntity.HINT_STATE);

        // Если состояние 0 - ничего не рисуем
        if (hintState == 0) return;

        // Выбираем текстуру в зависимости от стейта
        ResourceLocation currentTexture = HINT_1;
        if (hintState == 2) currentTexture = HINT_2;
        if (hintState == 3) currentTexture = HINT_3;

        poseStack.pushPose();

        // 1. Поднимаем над головой (настрой высоту под своего моба, если нужно)
        poseStack.translate(0.0D, 2.5D, 0.0D);

        // 2. Биллбординг (всегда смотрим на игрока)
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // 3. Масштаб
        poseStack.scale(0.03F, 0.03F, 0.03F);

        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(currentTexture));

        float halfWidth = 14.0F; // Если ширина твоих иконок тоже 28
        float height = 30.0F;    // Если высота 30
        int fullLight = 15728880;

        // 4. Отрисовка с нормалью (0, 1, 0) для игнорирования затенения от мира
        vertexconsumer.vertex(matrix4f, -halfWidth, 0, 0).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexconsumer.vertex(matrix4f, -halfWidth, height, 0).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexconsumer.vertex(matrix4f, halfWidth, height, 0).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexconsumer.vertex(matrix4f, halfWidth, 0, 0).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();

        poseStack.popPose();
    }
}