package com.benji.netherman.client.layer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.BlacksmithEntity;
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

public class BlacksmithHintLayer extends GeoRenderLayer<BlacksmithEntity> {

    private static final ResourceLocation HINT_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/blacksmith_hint.png");

    public BlacksmithHintLayer(GeoEntityRenderer<BlacksmithEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, BlacksmithEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!animatable.getEntityData().get(BlacksmithEntity.SHOW_HINT)) return;

        poseStack.pushPose();

        poseStack.translate(0.0D, 2.5D, 0.0D); 
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.03F, 0.03F, 0.03F);

        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(HINT_TEXTURE));

        float halfWidth = 14.0F;
        float height = 30.0F;
        int fullLight = 15728880;

        vertexconsumer.vertex(matrix4f, -halfWidth, 0, 0).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexconsumer.vertex(matrix4f, -halfWidth, height, 0).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexconsumer.vertex(matrix4f, halfWidth, height, 0).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexconsumer.vertex(matrix4f, halfWidth, 0, 0).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0.0F, 1.0F, 0.0F).endVertex();

        poseStack.popPose();
    }
}