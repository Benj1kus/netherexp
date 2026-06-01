package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.LaserModel;
import com.benji.netherman.entity.LaserEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LaserRenderer extends GeoEntityRenderer<LaserEntity> {
    public LaserRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LaserModel());
        this.shadowRadius = 0.0f; // У лазера не должно быть черной тени под собой

        // Подключаем светящийся (Emissive) слой
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/entity/laser_emissive.png");
        addRenderLayer(new GenericEmissiveLayer<>(this, emissiveTexture));
    }

    // --- ИСПРАВЛЕНИЕ НЕВИДИМОСТИ ---
    @Override
    public boolean shouldRender(LaserEntity entity, Frustum camera, double camX, double camY, double camZ) {
        // Отключаем Frustum Culling!
        // Теперь движок не будет скрывать лазер, даже если его "центр" вне экрана.
        return true;
    }
}