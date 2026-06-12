package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.EntranceBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.EntranceModel;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class EntranceRenderer extends GeoBlockRenderer<EntranceBlockEntity> {
    public EntranceRenderer(BlockEntityRendererProvider.Context context) {
        super(new EntranceModel());

        addRenderLayer(new GenericEmissiveLayer<>(this, new ResourceLocation(NetherExp.MODID, "textures/block/entrance_emissive.png")));
    }
    @Override
    public RenderType getRenderType(EntranceBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}