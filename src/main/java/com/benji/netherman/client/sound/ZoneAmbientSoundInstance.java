package com.benji.netherman.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;

public class ZoneAmbientSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private final MobEffect requiredEffect;

    public ZoneAmbientSoundInstance(SoundEvent soundEvent, Player player, MobEffect requiredEffect) {
        // SoundInstance.createUnseededRandom() используется для генерации параметров звука
        super(soundEvent, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.requiredEffect = requiredEffect;

        this.looping = true; // МАГИЯ! Майнкрафт сам идеально зациклит звук
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;

        // ВАЖНЫЕ НАСТРОЙКИ ДЛЯ ЗВУКА "В ГОЛОВЕ"
        this.relative = true; // Звук вездесущий, не привязан к координатам в мире
        this.attenuation = Attenuation.NONE; // Громкость не падает от расстояния
    }

    @Override
    public void tick() {
        // Если игрок умер, вышел из мира или ВЫПИЛ МОЛОКО (потерял эффект)
        if (this.player.isRemoved() || !this.player.isAlive() || !this.player.hasEffect(this.requiredEffect)) {
            // Мгновенно выключаем звук
            this.stop();
        }
    }
}