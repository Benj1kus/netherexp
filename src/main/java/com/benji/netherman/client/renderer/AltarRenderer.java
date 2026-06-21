package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.AltarBlockEntity;
import com.benji.netherman.block.entity.EyeBlockEntity;
import com.benji.netherman.client.layer.GlowmaskLayer;
import com.benji.netherman.client.model.AltarModel;
import com.benji.netherman.client.model.EyeModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class AltarRenderer extends GeoBlockRenderer<AltarBlockEntity> {
    public AltarRenderer(BlockEntityRendererProvider.Context context) {
        super(new AltarModel());
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/block/altar_emissive.png");
        addRenderLayer(new GlowmaskLayer<>(this, emissiveTexture));
    }
}