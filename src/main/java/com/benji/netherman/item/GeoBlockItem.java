package com.benji.netherman.item;

import com.benji.netherman.client.renderer.item.GeoBlockItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class GeoBlockItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animation;
    private final ResourceLocation emissiveTexture;

    public GeoBlockItem(Block block, Properties properties, ResourceLocation model, ResourceLocation texture, ResourceLocation animation, ResourceLocation emissiveTexture) {
        super(block, properties);
        this.model = model;
        this.texture = texture;
        this.animation = animation;
        this.emissiveTexture = emissiveTexture;
    }

    public ResourceLocation getGeoModel() { return model; }
    public ResourceLocation getGeoTexture() { return texture; }
    public ResourceLocation getGeoAnimation() { return animation; }
    public ResourceLocation getEmissiveTexture() { return emissiveTexture; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoBlockItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new GeoBlockItemRenderer(GeoBlockItem.this);
                }
                return renderer;
            }
        });
    }
}