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

    public static final RegistryObject<SoundEvent> UNIT_IDLE = registerSoundEvent("unit_idle");
    public static final RegistryObject<SoundEvent> SPINNING_WHEEL = registerSoundEvent("spinning_wheel");


    public static final RegistryObject<SoundEvent> BLACKSMITH_IDLE = registerSoundEvent("blacksmith_idle");

    public static final RegistryObject<SoundEvent> AZAZEL_VOICE = registerSoundEvent("azazel_voice");
    public static final RegistryObject<SoundEvent> CLOCK = registerSoundEvent("clock");
    public static final RegistryObject<SoundEvent> WHISPER = registerSoundEvent("whisper");
    public static final RegistryObject<SoundEvent> FLASH = registerSoundEvent("flash");


    public static final RegistryObject<SoundEvent> SNEEZE = registerSoundEvent("sneeze");
    public static final RegistryObject<SoundEvent> DOCTOR = registerSoundEvent("doctor");

    public static final RegistryObject<SoundEvent> SPRING_1 = registerSoundEvent("spring1");
    public static final RegistryObject<SoundEvent> SPRING_2 = registerSoundEvent("spring2");
    public static final RegistryObject<SoundEvent> SPRING_3 = registerSoundEvent("spring3");

    public static final RegistryObject<SoundEvent> STATUE_HURT_1 = registerSoundEvent("statue_hurt1");
    public static final RegistryObject<SoundEvent> STATUE_HURT_2 = registerSoundEvent("statue_hurt2");
    public static final RegistryObject<SoundEvent> STATUE_HURT_3 = registerSoundEvent("statue_hurt3");

    public static final RegistryObject<SoundEvent> GUARDIAN_WALK = registerSoundEvent("guardian_walk");
    public static final RegistryObject<SoundEvent> GRAND_DOOR_OPEN = registerSoundEvent("grand_door_open");
    public static final RegistryObject<SoundEvent> GRAND_DOOR_CLOSE = registerSoundEvent("grand_door_close");

    public static final RegistryObject<SoundEvent> ENTRANCE = registerSoundEvent("entrance");
    public static final RegistryObject<SoundEvent> DAMNED = registerSoundEvent("damned");
    public static final RegistryObject<SoundEvent> GOODLUCK = registerSoundEvent("goodluck");

    //BLOCKS

    public static final RegistryObject<SoundEvent> SAMSONIT_BREAK = registerSoundEvent("block.samsonit.break");
    public static final RegistryObject<SoundEvent> SAMSONIT_STEP = registerSoundEvent("block.samsonit.step");
    public static final RegistryObject<SoundEvent> SAMSONIT_PLACE = registerSoundEvent("block.samsonit.place");
    public static final RegistryObject<SoundEvent> SAMSONIT_HIT = registerSoundEvent("block.samsonit.hit");

    public static final RegistryObject<SoundEvent> SAMSONIT_BRICKS_STEP = registerSoundEvent("block.samsonit_bricks.step");
    public static final RegistryObject<SoundEvent> SAMSONIT_BRICKS_PLACE = registerSoundEvent("block.samsonit_bricks.place");

    public static final RegistryObject<SoundEvent> CAVE_AMBIENT = registerSoundEvent("cave_ambient");
    public static final RegistryObject<SoundEvent> CHURCH_AMBIENT = registerSoundEvent("church_ambient");
    public static final RegistryObject<SoundEvent> CITY_AMBIENT = registerSoundEvent("city_ambient");
    public static final RegistryObject<SoundEvent> RESPAWN_TOTEM = registerSoundEvent("respawn_totem");

    public static final RegistryObject<SoundEvent> BIG_TEXT = registerSoundEvent("big_text");

    public static final RegistryObject<SoundEvent> AZAZEL_IDLE_1 = registerSoundEvent("azazel_idle1");
    public static final RegistryObject<SoundEvent> AZAZEL_IDLE_2 = registerSoundEvent("azazel_idle2");
    public static final RegistryObject<SoundEvent> AZAZEL_IDLE_3 = registerSoundEvent("azazel_idle3");
    public static final RegistryObject<SoundEvent> AZAZEL_IDLE_4 = registerSoundEvent("azazel_idle4");

    public static final RegistryObject<SoundEvent> BOSS_FIGHT = registerSoundEvent("boss_fight");
    public static final RegistryObject<SoundEvent> BOSS_FIGHT_LOOP = registerSoundEvent("boss_fight_loop");

    public static final RegistryObject<SoundEvent> SPAWN_UNIT = registerSoundEvent("spawn_unit");

    public static final RegistryObject<SoundEvent> IDLE_PRAY = registerSoundEvent("idle_pray");
    public static final RegistryObject<SoundEvent> AZAZEL_PRAY = registerSoundEvent("azazel_pray");
    public static final RegistryObject<SoundEvent> AZAZEL_PHASE = registerSoundEvent("azazel_phase");
    public static final RegistryObject<SoundEvent> DEFENCE = registerSoundEvent("defence");
    public static final RegistryObject<SoundEvent> ARROW_ATTACK = registerSoundEvent("arrow_attack");
    public static final RegistryObject<SoundEvent> WHEEL_ATTACK = registerSoundEvent("wheel_attack");
    public static final RegistryObject<SoundEvent> BREATH_AZAZEL = registerSoundEvent("breath_azazel");


    public static final RegistryObject<SoundEvent> AZAZEL_DAMAGE_1 = registerSoundEvent("azazel_damage1");
    public static final RegistryObject<SoundEvent> AZAZEL_DAMAGE_2 = registerSoundEvent("azazel_damage2");

    public static final RegistryObject<SoundEvent> WING_1 = registerSoundEvent("wing1");
    public static final RegistryObject<SoundEvent> WING_2 = registerSoundEvent("wing2");
    public static final RegistryObject<SoundEvent> WING_3 = registerSoundEvent("wing3");

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