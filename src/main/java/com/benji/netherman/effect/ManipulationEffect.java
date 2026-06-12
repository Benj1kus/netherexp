package com.benji.netherman.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ManipulationEffect extends MobEffect {
    public ManipulationEffect() {
        super(MobEffectCategory.HARMFUL, 0x990000);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            AABB box = player.getBoundingBox().inflate(20.0);

            List<Piglin> piglins = player.level().getEntitiesOfClass(Piglin.class, box);
            for (Piglin piglin : piglins) {
                if (piglin.getTarget() != player) piglin.setTarget(player);
            }

            List<ZombifiedPiglin> zombiePiglins = player.level().getEntitiesOfClass(ZombifiedPiglin.class, box);
            for (ZombifiedPiglin zp : zombiePiglins) {
                if (zp.getTarget() != player) zp.setTarget(player);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}