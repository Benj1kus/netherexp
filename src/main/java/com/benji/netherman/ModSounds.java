package com.benji.netherman;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NetherExp.MODID);

    public static final RegistryObject<SoundEvent> GUARDIAN_NEUTRAL_1 = registerSoundEvent("guardian_neutral1");
    public static final RegistryObject<SoundEvent> GUARDIAN_NEUTRAL_2 = registerSoundEvent("guardian_neutral2");
    public static final RegistryObject<SoundEvent> GUARDIAN_NEUTRAL_3 = registerSoundEvent("guardian_neutral3");

    public static final RegistryObject<SoundEvent> GUARDIAN_WALK = registerSoundEvent("guardian_walk");
    public static final RegistryObject<SoundEvent> GRAND_DOOR_OPEN = registerSoundEvent("grand_door_open");
    public static final RegistryObject<SoundEvent> GRAND_DOOR_CLOSE = registerSoundEvent("grand_door_close");

    public static final RegistryObject<SoundEvent> ENTRANCE = registerSoundEvent("entrance");
    public static final RegistryObject<SoundEvent> DAMNED = registerSoundEvent("damned");
    public static final RegistryObject<SoundEvent> GOODLUCK = registerSoundEvent("goodluck");


    public static final RegistryObject<SoundEvent> GUARDIAN_IDLE_1 = registerSoundEvent("guardian_idle1");
    public static final RegistryObject<SoundEvent> GUARDIAN_IDLE_2 = registerSoundEvent("guardian_idle2");
    public static final RegistryObject<SoundEvent> GUARDIAN_IDLE_3 = registerSoundEvent("guardian_idle3");

    public static final RegistryObject<SoundEvent> PRISON_1 = registerSoundEvent("prison1");
    public static final RegistryObject<SoundEvent> PRISON_2 = registerSoundEvent("prison2");
    public static final RegistryObject<SoundEvent> PRISON_3 = registerSoundEvent("prison3");
    public static final RegistryObject<SoundEvent> PRISON_4 = registerSoundEvent("prison4");

    public static final RegistryObject<SoundEvent> GUARDIAN_ROAR_1 = registerSoundEvent("guardian_roar1");
    public static final RegistryObject<SoundEvent> GUARDIAN_ROAR_2 = registerSoundEvent("guardian_roar2");
    public static final RegistryObject<SoundEvent> GUARDIAN_ROAR_3 = registerSoundEvent("guardian_roar3");
    public static final RegistryObject<SoundEvent> WEAKNESS = registerSoundEvent("weakness");

    public static final RegistryObject<SoundEvent> SPEC_ATTACK_1 = registerSoundEvent("spec_attack1");
    public static final RegistryObject<SoundEvent> SPEC_ATTACK_2 = registerSoundEvent("spec_attack2");
    public static final RegistryObject<SoundEvent> SPEC_ATTACK_3 = registerSoundEvent("spec_attack3");

    public static final RegistryObject<SoundEvent> SUMMON1 = registerSoundEvent("summon1");
    public static final RegistryObject<SoundEvent> SUMMON2 = registerSoundEvent("summon2");

    public static final RegistryObject<SoundEvent> GUARDIAN_DAMAGE_1 = registerSoundEvent("guardian_damage1");
    public static final RegistryObject<SoundEvent> GUARDIAN_DAMAGE_2 = registerSoundEvent("guardian_damage2");

    public static final RegistryObject<SoundEvent> GHASTLY_IDLE = registerSoundEvent("ghastly_idle");
    public static final RegistryObject<SoundEvent> GHASTLY_HURT_1 = registerSoundEvent("ghastly_hurt1");
    public static final RegistryObject<SoundEvent> GHASTLY_HURT_2 = registerSoundEvent("ghastly_hurt2");
    public static final RegistryObject<SoundEvent> GHASTLY_HURT_3 = registerSoundEvent("ghastly_hurt3");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(NetherExp.MODID, name)));
    }
}