package com.benji.netherman.client.layer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.TraphiveBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class TraphiveAnimatedEmissiveLayer extends GeoRenderLayer<TraphiveBlockEntity> {

    // Кэшируем локации всех 7 текстур один раз при запуске игры, чтобы не нагружать память
    private static final ResourceLocation[] FRAMES = new ResourceLocation[7];
    static {
        for (int i = 0; i < 7; i++) {
            FRAMES[i] = new ResourceLocation(NetherExp.MODID, "textures/block/traphive_emissive_" + i + ".png");
        }
    }

    public TraphiveAnimatedEmissiveLayer(GeoBlockRenderer<TraphiveBlockEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, TraphiveBlockEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (animatable.getLevel() == null) return;

        // 1. Получаем точное время с учетом partialTick (для плавности на высокой частоте кадров монитора)
        float time = animatable.getLevel().getGameTime() + partialTick;

        // СКОРОСТЬ АНИМАЦИИ: 4 тика на смену одного кадра.
        // (Если хочешь быстрее - поставь 2.0F или 3.0F. Если медленнее - 5.0F)
        float speed = 3.0F;
        float exactFrame = time / speed;

        // 2. Вычисляем индексы: какой кадр сейчас, и какой будет следующим
        int currentFrame = (int) Math.floor(exactFrame) % 7;
        int nextFrame = (currentFrame + 1) % 7;

        // 3. Вычисляем процент смешивания (от 0.0 до 1.0)
        float blendFactor = exactFrame - (float) Math.floor(exactFrame);

        // 4. Отрисовываем ТЕКУЩИЙ кадр (он плавно угасает)
        RenderType currentRenderType = RenderType.entityTranslucentEmissive(FRAMES[currentFrame]);
        VertexConsumer currentBuffer = bufferSource.getBuffer(currentRenderType);
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, currentRenderType, currentBuffer, partialTick,
                15728880, // Магическое число максимального свечения в темноте
                packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F - blendFactor); // Канал Alpha (прозрачность) падает от 1.0 до 0.0

        // 5. Отрисовываем СЛЕДУЮЩИЙ кадр поверх (он плавно появляется)
        RenderType nextRenderType = RenderType.entityTranslucentEmissive(FRAMES[nextFrame]);
        VertexConsumer nextBuffer = bufferSource.getBuffer(nextRenderType);
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, nextRenderType, nextBuffer, partialTick,
                15728880,
                packedOverlay,
                1.0F, 1.0F, 1.0F, blendFactor); // Канал Alpha (прозрачность) растет от 0.0 до 1.0
    }
}