package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.LabyrinthTeleportBlock;
import com.benji.netherman.block.entity.LabyrinthTeleportBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LabyrinthTeleportRenderer implements BlockEntityRenderer<LabyrinthTeleportBlockEntity> {

    public static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/azazel_beacon_beam.png");

    public LabyrinthTeleportRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LabyrinthTeleportBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        if (blockEntity.getBlockState().hasProperty(LabyrinthTeleportBlock.MODE)) {
            if (blockEntity.getBlockState().getValue(LabyrinthTeleportBlock.MODE) == 1) { 

                long gameTime = blockEntity.getLevel().getGameTime();

                
                BeaconRenderer.renderBeaconBeam(
                        poseStack,
                        bufferSource,
                        BEAM_TEXTURE,
                        partialTick,
                        1.0F,           
                        gameTime,       
                        0,              
                        256,            
                        new float[]{1.0F, 1.0F, 1.0F}, 
                        0.2F,           
                        0.25F           
                );
            }
        }
    }

    
    @Override
    public int getViewDistance() {
        return 256;
    }
}