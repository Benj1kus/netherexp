package com.benji.netherman.client.model;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelSpikesProjectileEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AzazelSpikeProjectileModel extends GeoModel<AzazelSpikesProjectileEntity> {
    @Override
    public ResourceLocation getModelResource(AzazelSpikesProjectileEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "geo/azazel_spikes_projectile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AzazelSpikesProjectileEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "textures/entity/azazel_spikes_projectile.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AzazelSpikesProjectileEntity animatable) {
        return new ResourceLocation(NetherExp.MODID, "animations/empty.animation.json");
    }
}