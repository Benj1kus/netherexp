package com.benji.netherman.client.renderer;

import com.benji.netherman.client.layer.BelieverPrayEmissiveLayer;
import com.benji.netherman.client.model.BelieverModel;
import com.benji.netherman.client.model.BelieverVillagerModel;
import com.benji.netherman.entity.BelieverEntity;
import com.benji.netherman.entity.BelieverVillagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BelieverVillagerRenderer extends GeoEntityRenderer<BelieverVillagerEntity> {
    public BelieverVillagerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BelieverVillagerModel());
        this.shadowRadius = 0.5f;
    }
}