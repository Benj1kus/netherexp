package com.benji.netherman.client.layer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.BelieverEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BelieverPrayEmissiveLayer extends GeoRenderLayer<BelieverEntity> {
    private static final ResourceLocation EMISSIVE = new ResourceLocation(NetherExp.MODID, "textures/entity/believer_pray_emissive.png");

    public BelieverPrayEmissiveLayer(GeoEntityRenderer<BelieverEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, BelieverEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (animatable.getEntityData().get(BelieverEntity.IS_PROTECTED)) {
            RenderType emissiveRenderType = RenderType.eyes(EMISSIVE);

            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, emissiveRenderType, bufferSource.getBuffer(emissiveRenderType), partialTick, 15728880, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}