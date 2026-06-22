package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.LabyrinthTeleportBlock;
import com.benji.netherman.block.entity.LabyrinthTeleportBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LabyrinthTeleportRenderer implements BlockEntityRenderer<LabyrinthTeleportBlockEntity> {

    public static final ResourceLocation BEAM_TEXTURE = new ResourceLocation(NetherExp.MODID, "textures/block/azazel_beacon_beam.png");

    public LabyrinthTeleportRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LabyrinthTeleportBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Проверяем состояние блока
        if (blockEntity.getBlockState().hasProperty(LabyrinthTeleportBlock.MODE)) {
            if (blockEntity.getBlockState().getValue(LabyrinthTeleportBlock.MODE) == 1) { // 1 = Активен

                long gameTime = blockEntity.getLevel().getGameTime();

                // Используем ванильный генератор луча для 1.20.1
                BeaconRenderer.renderBeaconBeam(
                        poseStack,
                        bufferSource,
                        BEAM_TEXTURE,
                        partialTick,
                        1.0F,           // Масштаб текстуры
                        gameTime,       // Время для анимации вращения
                        0,              // Смещение по Y (откуда начинается луч)
                        256,            // Высота луча
                        new float[]{1.0F, 1.0F, 1.0F}, // <--- ИСПРАВЛЕНО: Массив RGB для белого цвета
                        0.2F,           // Внутренний радиус луча
                        0.25F           // Внешний радиус луча (Свечение)
                );
            }
        }
    }

    // Дальность видимости луча (256 блоков)
    @Override
    public int getViewDistance() {
        return 256;
    }
}