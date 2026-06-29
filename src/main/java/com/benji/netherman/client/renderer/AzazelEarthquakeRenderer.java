package com.benji.netherman.client.renderer;

import com.benji.netherman.client.layer.BelieverPrayEmissiveLayer;
import com.benji.netherman.client.model.AzazelEarthquakeModel;
import com.benji.netherman.client.model.BelieverModel;
import com.benji.netherman.entity.AzazelEarthquakeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AzazelEarthquakeRenderer extends GeoEntityRenderer<AzazelEarthquakeEntity> {
    public AzazelEarthquakeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AzazelEarthquakeModel());
    }
}