package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.VoidNetherMidBlockEntity;
import com.benji.netherman.block.entity.VoidNetherMidCornerBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.VoidNetherMidCornerModel;
import com.benji.netherman.client.model.VoidNetherMidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class VoidNetherMidCornerRenderer extends GeoBlockRenderer<VoidNetherMidCornerBlockEntity> {
    public VoidNetherMidCornerRenderer(BlockEntityRendererProvider.Context context) {
        super(new VoidNetherMidCornerModel());
        addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(NetherExp.MODID, "textures/block/void_nether_emissive.png")));
    }
}