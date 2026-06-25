package com.benji.netherman.client.renderer;

import com.benji.netherman.client.model.BellGuardianModel;
import com.benji.netherman.entity.BellGuardianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BellGuardianRenderer extends GeoEntityRenderer<BellGuardianEntity> {
    public BellGuardianRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BellGuardianModel());
        this.shadowRadius = 0.5f;
    }
}