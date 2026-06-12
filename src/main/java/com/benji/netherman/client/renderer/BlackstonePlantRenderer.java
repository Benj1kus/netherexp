package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.BlackstonePlantBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.BlackstonePlantModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BlackstonePlantRenderer extends GeoBlockRenderer<BlackstonePlantBlockEntity> {
    public BlackstonePlantRenderer(BlockEntityRendererProvider.Context context) {
        super(new BlackstonePlantModel());
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_plant_emissive.png");
        addRenderLayer(new GenericEmissiveLayer<>(this, emissiveTexture));
    }
}