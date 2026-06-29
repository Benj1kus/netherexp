package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.layer.GlowmaskLayer;
import com.benji.netherman.client.model.AzazelSplashModel;
import com.benji.netherman.entity.AzazelSplashEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AzazelSplashRenderer extends GeoEntityRenderer<AzazelSplashEntity> {
    public AzazelSplashRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AzazelSplashModel());
        this.shadowRadius = 0.0f; 
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/entity/splash_emissive.png");
        addRenderLayer(new GlowmaskLayer<>(this, emissiveTexture));
    }

    @Override
    protected int getBlockLightLevel(AzazelSplashEntity entity, BlockPos pos) {
        return 15; 
    }
}