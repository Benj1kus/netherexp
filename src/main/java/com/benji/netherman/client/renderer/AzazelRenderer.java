package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.layer.GlowmaskLayer;
import com.benji.netherman.client.model.AzazelModel;
import com.benji.netherman.entity.AzazelEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AzazelRenderer extends GeoEntityRenderer<AzazelEntity> {
    public AzazelRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AzazelModel());
        this.shadowRadius = 1.5f;
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_emissive.png");
        addRenderLayer(new GlowmaskLayer<>(this, emissiveTexture));
    }
}