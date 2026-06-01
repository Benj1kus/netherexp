package com.benji.netherman.client.renderer;

import com.benji.netherman.client.layer.DoctorHintLayer;
import com.benji.netherman.client.model.DoctorModel;
import com.benji.netherman.entity.DoctorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DoctorRenderer extends GeoEntityRenderer<DoctorEntity> {
    public DoctorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DoctorModel());
        this.shadowRadius = 0.5f;

        // Подключаем слой всплывающих подсказок
        addRenderLayer(new DoctorHintLayer(this));
    }
}