package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.model.ManipulatorModel;
import com.benji.netherman.client.layer.GenericEmissiveLayer; // Твой кастомный слой свечения
import com.benji.netherman.entity.ManipulatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ManipulatorRenderer extends GeoEntityRenderer<ManipulatorEntity> {
    public ManipulatorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ManipulatorModel());

        // Подключаем светящуюся текстуру глаз/узоров
        addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(NetherExp.MODID, "textures/entity/manipulator_emissive.png")));
    }
}