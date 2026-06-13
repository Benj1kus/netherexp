package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.layer.GlowmaskLayer;
import com.benji.netherman.client.model.GrandDoorModel;
import com.benji.netherman.block.entity.GrandDoorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GrandDoorRenderer extends GeoBlockRenderer<GrandDoorBlockEntity> {
    public GrandDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new GrandDoorModel());
        addRenderLayer(new GlowmaskLayer<>(this, new ResourceLocation(NetherExp.MODID, "textures/block/grand_door_emissive.png")));
    }
}