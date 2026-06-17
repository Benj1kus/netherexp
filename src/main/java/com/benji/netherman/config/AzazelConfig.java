package com.benji.netherman.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AzazelConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue KNOCKBACK_RESISTANCE;
    public static final ForgeConfigSpec.DoubleValue LAUNCH_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue PULL_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue WIND_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue WHEEL_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.IntValue ATTACK_CHANCE;
    public static final ForgeConfigSpec.IntValue PASSIVE_SUMMON_CHANCE;

    public static final ForgeConfigSpec.IntValue MASK_REGEN_COOLDOWN;
    public static final ForgeConfigSpec.BooleanValue MASK_FIRE_IMMUNITY;

    public static final ForgeConfigSpec.DoubleValue PLAYER_DETECTION_RADIUS;
    public static final ForgeConfigSpec.IntValue MINI_BOSS_COOLDOWN;
    public static final ForgeConfigSpec.IntValue CIVILIAN_NPC_COOLDOWN;
    public static final ForgeConfigSpec.IntValue BELIEVERS_SPAWN_COUNT;
    public static final ForgeConfigSpec.DoubleValue BELIEVERS_SPAWN_RADIUS;
    public static final ForgeConfigSpec.IntValue BELIEVERS_MAX_NEARBY;
    public static final ForgeConfigSpec.IntValue BELIEVERS_SUCCESS_COOLDOWN;
    public static final ForgeConfigSpec.IntValue BELIEVERS_FAIL_COOLDOWN;

    public static final ForgeConfigSpec.DoubleValue MELEE_ATTACK_RADIUS;
    public static final ForgeConfigSpec.IntValue SHIELD_HITS_MIN;
    public static final ForgeConfigSpec.IntValue SHIELD_HITS_MAX;

    public static final ForgeConfigSpec.IntValue MIDAS_GUARDIAN_COUNT;
    public static final ForgeConfigSpec.IntValue MIDAS_BOSSUNIT_COUNT;
    public static final ForgeConfigSpec.DoubleValue MIDAS_FIRE_DAMAGE;
    public static final ForgeConfigSpec.IntValue MIDAS_GOLD_TIME;
    public static final ForgeConfigSpec.IntValue MELEE_ATTACK_CHANCE;

    public static final ForgeConfigSpec.IntValue PRISON_RADIUS;
    public static final ForgeConfigSpec.IntValue PRISON_DURATION;
    public static final ForgeConfigSpec.IntValue PRISON_MAX_HEIGHT;

    static {

        // boss
        BUILDER.push("Azazel Boss Configuration");
        MAX_HEALTH = BUILDER.comment("Maximum health of Azazel").defineInRange("maxHealth", 800.0, 100.0, 10000.0);
        MOVEMENT_SPEED = BUILDER.comment("Movement speed of Azazel").defineInRange("movementSpeed", 0.2, 0.05, 1.0);
        KNOCKBACK_RESISTANCE = BUILDER.comment("Knockback resistance (1.0 = completely immune)").defineInRange("knockbackResistance", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("Azazel Attack Damage");
        LAUNCH_ATTACK_DAMAGE = BUILDER.comment("Damage dealt by the launch explosion attack").defineInRange("launchAttackDamage", 8.0, 0.0, 100.0);
        PULL_ATTACK_DAMAGE = BUILDER.comment("Damage dealt when pulling players in").defineInRange("pullAttackDamage", 5.0, 0.0, 100.0);
        WIND_ATTACK_DAMAGE = BUILDER.comment("Damage dealt by the wind knockback attack").defineInRange("windAttackDamage", 5.0, 0.0, 100.0);
        WHEEL_ATTACK_DAMAGE = BUILDER.comment("Damage dealt per hit during the wheel dash attack").defineInRange("wheelAttackDamage", 4.0, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("Azazel Attack Frequencies");
        ATTACK_CHANCE = BUILDER.comment("Chance (1 in X ticks) for Azazel to perform an active attack. Lower = faster attacks.").defineInRange("attackChance", 80, 10, 600);
        PASSIVE_SUMMON_CHANCE = BUILDER.comment("Chance (1 in X ticks) to spawn minions passively while idle.").defineInRange("passiveSummonChance", 600, 100, 2400);
        BUILDER.pop();

        // spawner
        BUILDER.push("Nether Spawner Configuration");

        PLAYER_DETECTION_RADIUS = BUILDER.comment("Radius within which the spawner detects players to activate.")
                .defineInRange("playerDetectionRadius", 20.0, 1.0, 128.0);

        MINI_BOSS_COOLDOWN = BUILDER.comment("Cooldown (in ticks) for spawning Mini-Bosses (Manipulator, Guardian, Welcomer) [20 ticks = 1 second].")
                .defineInRange("miniBossCooldown", 18000, 1200, 1000000);

        CIVILIAN_NPC_COOLDOWN = BUILDER.comment("Cooldown (in ticks) for spawning Civilian NPCs (Blacksmith, Doctor, Gilded Golem, Trader).")
                .defineInRange("civilianNpcCooldown", 72000, 1200, 1000000);

        BELIEVERS_SPAWN_COUNT = BUILDER.comment("Number of Believers spawned at once.")
                .defineInRange("believersSpawnCount", 5, 1, 20);

        BELIEVERS_SPAWN_RADIUS = BUILDER.comment("The scatter radius for spawning Believers.")
                .defineInRange("believersSpawnRadius", 6.0, 1.0, 32.0);

        BELIEVERS_MAX_NEARBY = BUILDER.comment("Maximum number of Believers around the spawner before it skips spawning.")
                .defineInRange("believersMaxNearby", 5, 1, 50);

        BELIEVERS_SUCCESS_COOLDOWN = BUILDER.comment("Cooldown if Believers successfully spawned.")
                .defineInRange("believersSuccessCooldown", 72000, 1200, 1000000);

        BELIEVERS_FAIL_COOLDOWN = BUILDER.comment("Soft cooldown if spawning was skipped because there are too many Believers around.")
                .defineInRange("believersFailCooldown", 600, 20, 72000);

        BUILDER.pop();

        BUILDER.push("Azazel Trophy Mask Configuration");
        MASK_REGEN_COOLDOWN = BUILDER.comment("Time (in ticks) to regenerate 1 totem charge in the mask [2400 ticks = 2 minutes].")
                .defineInRange("maskRegenCooldown", 2400, 200, 72000);

        MASK_FIRE_IMMUNITY = BUILDER.comment("Does wearing the Azazel Trophy mask grant fire immunity?")
                .define("maskFireImmunity", true);
        BUILDER.pop();

        BUILDER.push("Azazel Defense & Melee");
        MELEE_ATTACK_RADIUS = BUILDER.comment("Radius to trigger close-combat attacks (Launch, Midas)").defineInRange("meleeAttackRadius", 6.0, 1.0, 20.0);
        MELEE_ATTACK_CHANCE = BUILDER.comment("Chance (0-100%) to perform a melee attack when a player is close").defineInRange("meleeAttackChance", 30, 0, 100);
        SHIELD_HITS_MIN = BUILDER.comment("Minimum hits required to trigger the defense shield").defineInRange("shieldHitsMin", 5, 1, 100);
        SHIELD_HITS_MAX = BUILDER.comment("Maximum hits required to trigger the defense shield").defineInRange("shieldHitsMax", 20, 1, 100);
        BUILDER.pop();

        BUILDER.push("Azazel Midas Attack");
        MIDAS_BOSSUNIT_COUNT = BUILDER.comment("Number of BossUnits spawned during Midas attack").defineInRange("midasBossUnitCount", 3, 0, 10);
        MIDAS_GUARDIAN_COUNT = BUILDER.comment("Number of Guardians spawned during Midas attack").defineInRange("midasGuardianCount", 2, 0, 10);
        MIDAS_FIRE_DAMAGE = BUILDER.comment("Fire damage dealt per tick on the fire ring").defineInRange("midasFireDamage", 8.0, 0.0, 100.0);
        MIDAS_GOLD_TIME = BUILDER.comment("Ticks required near the boss to turn an item into gold (40 = 2 seconds)").defineInRange("midasGoldTime", 40, 1, 600);
        BUILDER.pop();

        BUILDER.push("Azazel Prison Attack");
        PRISON_RADIUS = BUILDER.comment("Radius of the blackstone prison").defineInRange("prisonRadius", 5, 2, 15);
        PRISON_DURATION = BUILDER.comment("Time in ticks before the prison breaks (200 = 10 seconds)").defineInRange("prisonDuration", 200, 60, 1200);
        PRISON_MAX_HEIGHT = BUILDER.comment("Maximum random height of the prison walls").defineInRange("prisonMaxHeight", 6, 3, 15);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}