package com.benji.netherman.client.renderer.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.layer.GuardianEmissiveLayer;
import com.benji.netherman.client.model.GuardianModel;
import com.benji.netherman.entity.GuardianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GuardianRenderer extends GeoEntityRenderer<GuardianEntity> {
    public GuardianRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GuardianModel());
        this.shadowRadius = 1.0f;

        addRenderLayer(new GuardianEmissiveLayer(this, new ResourceLocation(NetherExp.MODID, "textures/entity/guardian_emissive.png")));
    }
}