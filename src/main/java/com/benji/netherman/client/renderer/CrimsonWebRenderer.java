package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.CrimsonWebBlockEntity;
import com.benji.netherman.block.entity.EntranceBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.CrimsonWebModel;
import com.benji.netherman.client.model.EntranceModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CrimsonWebRenderer extends GeoBlockRenderer<CrimsonWebBlockEntity> {
    public CrimsonWebRenderer(BlockEntityRendererProvider.Context context) {
        super(new CrimsonWebModel());

        addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_column_emissive.png")));
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {

        switch (facing) {

            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));

            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));

            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));

            default -> {}
        }
    }
    @Override
    public RenderType getRenderType(CrimsonWebBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}