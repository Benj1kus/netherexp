package com.benji.netherman.client.renderer.entity;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.CrimsonArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CrimsonArrowRenderer extends ArrowRenderer<CrimsonArrowEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/entity/crimson_arrow.png");

    public CrimsonArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CrimsonArrowEntity entity) {
        return TEXTURE;
    }
}