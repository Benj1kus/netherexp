package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.layer.GlowmaskLayer;
import com.benji.netherman.client.model.AzazelSpikeModel;
import com.benji.netherman.entity.AzazelSpikeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AzazelSpikeRenderer extends GeoEntityRenderer<AzazelSpikeEntity> {
    public AzazelSpikeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AzazelSpikeModel());
        this.shadowRadius = 0.0f; 
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_spikes_emissive.png");
        addRenderLayer(new GlowmaskLayer<>(this, emissiveTexture));
    }

    @Override
    protected int getBlockLightLevel(AzazelSpikeEntity entity, BlockPos pos) {
        return 15; 
    }
}