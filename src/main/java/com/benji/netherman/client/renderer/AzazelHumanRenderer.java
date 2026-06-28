package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.layer.GlowmaskLayer;
import com.benji.netherman.client.model.AzazelHumanModel;
import com.benji.netherman.client.model.AzazelModel;
import com.benji.netherman.entity.AzazelEntity;
import com.benji.netherman.entity.AzazelHumanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AzazelHumanRenderer extends GeoEntityRenderer<AzazelHumanEntity> {
    public AzazelHumanRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AzazelHumanModel());
        this.shadowRadius = 1.5f;
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_human_emissive.png");
        addRenderLayer(new GlowmaskLayer<>(this, emissiveTexture));
    }
}