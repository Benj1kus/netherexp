package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.model.VillagerPrisonerModel;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.entity.VillagerPrisonerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VillagerPrisonerRenderer extends GeoEntityRenderer<VillagerPrisonerEntity> {
    public VillagerPrisonerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VillagerPrisonerModel());
    }
}