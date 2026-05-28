package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.NetherSpawnerBlockEntity;
import com.benji.netherman.client.layer.GenericEmissiveLayer;
import com.benji.netherman.client.model.NetherSpawnerModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class NetherSpawnerRenderer extends GeoBlockRenderer<NetherSpawnerBlockEntity> {
    public NetherSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        super(new NetherSpawnerModel());

        // Указываем путь к твоему светящемуся слою
        ResourceLocation emissiveTexture = new ResourceLocation(NetherExp.MODID, "textures/block/nether_spawner_emissive.png");

        // Добавляем слой из твоего старого проекта
        addRenderLayer(new GenericEmissiveLayer<>(this, emissiveTexture));
    }
}