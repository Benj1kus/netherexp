package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.block.entity.PointedBlackstoneBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import software.bernie.geckolib.model.GeoModel;

public class PointedBlackstoneModel extends GeoModel<PointedBlackstoneBlockEntity> {
    @Override
    public ResourceLocation getModelResource(PointedBlackstoneBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/pointed_blackstone.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PointedBlackstoneBlockEntity animatable) {
        BlockState state = animatable.getBlockState();

        // Защита: проверяем, что у блока есть нужные свойства
        if (state.hasProperty(PointedDripstoneBlock.TIP_DIRECTION) && state.hasProperty(PointedDripstoneBlock.THICKNESS)) {
            Direction dir = state.getValue(PointedDripstoneBlock.TIP_DIRECTION);
            DripstoneThickness thickness = state.getValue(PointedDripstoneBlock.THICKNESS);

            // "down" или "up"
            String dirStr = dir == Direction.DOWN ? "down" : "up";

            // "base", "frustum", "middle", "tip", "merge"
            String thickStr = thickness.getSerializedName();

            // Майнкрафт называет соединение "merge", а твоя текстура "tip_merge". Исправляем:
            if (thickStr.equals("merge")) {
                thickStr = "tip_merge";
            }

            // Итоговый путь: "netherman:textures/block/pointed_blackstone_down_base.png" и т.д.
            return new ResourceLocation(NetherExp.MODID, "textures/block/pointed_blackstone_" + dirStr + "_" + thickStr + ".png");
        }

        return new ResourceLocation(NetherExp.MODID, "textures/block/pointed_blackstone_down_base.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PointedBlackstoneBlockEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/empty.animation.json");
    }
}