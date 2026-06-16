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

    public static final ForgeConfigSpec.DoubleValue PLAYER_DETECTION_RADIUS;
    public static final ForgeConfigSpec.IntValue MINI_BOSS_COOLDOWN;
    public static final ForgeConfigSpec.IntValue CIVILIAN_NPC_COOLDOWN;
    public static final ForgeConfigSpec.IntValue BELIEVERS_SPAWN_COUNT;
    public static final ForgeConfigSpec.DoubleValue BELIEVERS_SPAWN_RADIUS;
    public static final ForgeConfigSpec.IntValue BELIEVERS_MAX_NEARBY;
    public static final ForgeConfigSpec.IntValue BELIEVERS_SUCCESS_COOLDOWN;
    public static final ForgeConfigSpec.IntValue BELIEVERS_FAIL_COOLDOWN;

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
        SPEC = BUILDER.build();
    }
}