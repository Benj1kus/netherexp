package com.benji.netherman.client.renderer;

import com.benji.netherman.NetherExp;
import com.benji.netherman.client.model.TotemusPuzzleModel;
import com.benji.netherman.entity.TotemusPuzzleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TotemusPuzzleRenderer extends GeoEntityRenderer<TotemusPuzzleEntity> {

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            new ResourceLocation(NetherExp.MODID, "textures/entity/totemus_puzzle_red.png"),    
            new ResourceLocation(NetherExp.MODID, "textures/entity/totemus_puzzle_orange.png"), 
            new ResourceLocation(NetherExp.MODID, "textures/entity/totemus_puzzle_yellow.png"), 
            new ResourceLocation(NetherExp.MODID, "textures/entity/totemus_puzzle_green.png"),  
            new ResourceLocation(NetherExp.MODID, "textures/entity/totemus_puzzle_blue.png"),   
            new ResourceLocation(NetherExp.MODID, "textures/entity/totemus_puzzle_purple.png")  
    };

    public TotemusPuzzleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TotemusPuzzleModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public ResourceLocation getTextureLocation(TotemusPuzzleEntity animatable) {
        int colorIndex = animatable.getColor();
        if (colorIndex < 0 || colorIndex > 5) colorIndex = 0;

        return TEXTURES[colorIndex];
    }
}