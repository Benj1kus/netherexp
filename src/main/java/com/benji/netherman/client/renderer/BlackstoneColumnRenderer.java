package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.BlackstoneColumnBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.BlackstoneColumnModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BlackstoneColumnRenderer extends GeoBlockRenderer<BlackstoneColumnBlockEntity> {
    public BlackstoneColumnRenderer(BlockEntityRendererProvider.Context context) {
        super(new BlackstoneColumnModel());
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/block/blackstone_column_emissive.png");
        addRenderLayer(new GenericEmissiveLayer<>(this, emissiveTexture));
    }

    @Override
    public boolean shouldRenderOffScreen(BlackstoneColumnBlockEntity blockEntity) {
        return true;
    }
}