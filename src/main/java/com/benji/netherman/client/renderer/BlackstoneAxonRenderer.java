package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.BlackstoneAxonBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.BlackstoneAxonModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BlackstoneAxonRenderer extends GeoBlockRenderer<BlackstoneAxonBlockEntity> {
    public BlackstoneAxonRenderer(BlockEntityRendererProvider.Context context) {
        super(new BlackstoneAxonModel());
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_axon_emissive.png");
        addRenderLayer(new GenericEmissiveLayer<>(this, emissiveTexture));
    }
}