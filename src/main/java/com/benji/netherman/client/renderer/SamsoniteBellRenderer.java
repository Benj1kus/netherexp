package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.SamsoniteBellBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SamsoniteBellRenderer implements BlockEntityRenderer<SamsoniteBellBlockEntity> {
    // В 1.21.1: ResourceLocation.fromNamespaceAndPath(NetherExp.MODID, "textures/entity/samsonite_bell.png");
    public static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/samsonite_bell.png");
    private final ModelPart bellBody;

    public SamsoniteBellRenderer(BlockEntityRendererProvider.Context context) {
        this.bellBody = context.bakeLayer(ModelLayers.BELL).getChild("bell_body");
    }

    @Override
    public void render(SamsoniteBellBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float f = (float)entity.ticks + partialTicks;
        float rotX = 0.0F;
        float rotZ = 0.0F;

        if (entity.shaking) {
            float swing = Mth.sin(f / (float)Math.PI) / (4.0F + f / 3.0F);
            if (entity.clickDirection == Direction.NORTH) { rotX = -swing; }
            else if (entity.clickDirection == Direction.SOUTH) { rotX = swing; }
            else if (entity.clickDirection == Direction.EAST) { rotZ = -swing; }
            else if (entity.clickDirection == Direction.WEST) { rotZ = swing; }
        }

        this.bellBody.xRot = rotX;
        this.bellBody.zRot = rotZ;

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entitySolid(TEXTURE));
        this.bellBody.render(poseStack, vertexconsumer, packedLight, packedOverlay);
    }
}