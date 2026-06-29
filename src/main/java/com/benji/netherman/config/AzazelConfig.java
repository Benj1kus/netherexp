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

    public static final ForgeConfigSpec.DoubleValue HUMAN_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue HUMAN_MOVEMENT_SPEED;
    public static final ForgeConfigSpec.DoubleValue HUMAN_KNOCKBACK_RESISTANCE;

    public static final ForgeConfigSpec.DoubleValue HUMAN_STOMP_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HUMAN_STOMP_KNOCKBACK;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SCYTHE_1_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SCYTHE_1_KNOCKBACK;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SCYTHE_2_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SCYTHE_2_KNOCKBACK;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SPEAR_MELEE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SPEAR_MELEE_KNOCKBACK;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SPEAR_MID_PUSH_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SPEAR_MID_PUSH_KNOCKBACK;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SPEAR_MID_PULL_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SPEAR_MID_PULL_KNOCKBACK;
    public static final ForgeConfigSpec.DoubleValue HUMAN_CHARGE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HUMAN_CHARGE_KNOCKBACK;

    public static final ForgeConfigSpec.DoubleValue HUMAN_SPLASH_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue HUMAN_SPIKE_DAMAGE;
    public static final ForgeConfigSpec.IntValue HUMAN_SPIKE_WITHER_DURATION;
    public static final ForgeConfigSpec.DoubleValue HUMAN_PROJECTILE_SPIKE_DAMAGE;
    public static final ForgeConfigSpec.IntValue HUMAN_PROJECTILE_SPIKE_WITHER_DURATION;

    public static final ForgeConfigSpec.DoubleValue HUMAN_MELEE_ATTACK_RADIUS;
    public static final ForgeConfigSpec.DoubleValue HUMAN_MID_ATTACK_RADIUS_MAX;
    public static final ForgeConfigSpec.DoubleValue HUMAN_LONG_ATTACK_RADIUS_MIN;
    public static final ForgeConfigSpec.DoubleValue HUMAN_MOVEMENT_STOP_RADIUS;

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

        
        BUILDER.push("Azazel Boss Configuration");
        MAX_HEALTH = BUILDER.comment("Maximum health of Azazel").defineInRange("maxHealth", 800.0, 100.0, 10000.0);
        MOVEMENT_SPEED = BUILDER.comment("Movement speed of Azazel").defineInRange("movementSpeed", 0.2, 0.05, 1.0);
        KNOCKBACK_RESISTANCE = BUILDER.comment("Knockback resistance (1.0 = completely immune)").defineInRange("knockbackResistance", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("Azazel Human Configuration");
        HUMAN_MAX_HEALTH = BUILDER.comment("Maximum health of Azazel Human").defineInRange("humanMaxHealth", 1000.0, 100.0, 10000.0);
        HUMAN_MOVEMENT_SPEED = BUILDER.comment("Movement speed of Azazel Human").defineInRange("humanMovementSpeed", 0.25, 0.05, 1.0);
        HUMAN_KNOCKBACK_RESISTANCE = BUILDER.comment("Knockback resistance (1.0 = completely immune)").defineInRange("humanKnockbackResistance", 1.0, 0.0, 1.0);
        BUILDER.pop();

        // attacks
        BUILDER.push("Azazel Human Attack Config");
        HUMAN_STOMP_DAMAGE = BUILDER.comment("Damage for the leg stomp attack").defineInRange("humanStompDamage", 25.0, 0.0, 200.0);
        HUMAN_STOMP_KNOCKBACK = BUILDER.comment("Knockback strength for the leg stomp attack").defineInRange("humanStompKnockback", 1.2, 0.0, 10.0);

        HUMAN_SCYTHE_1_DAMAGE = BUILDER.comment("Damage for the first scythe swing").defineInRange("humanScythe1Damage", 15.0, 0.0, 200.0);
        HUMAN_SCYTHE_1_KNOCKBACK = BUILDER.comment("Knockback for the first scythe swing").defineInRange("humanScythe1Knockback", 0.5, 0.0, 10.0);
        HUMAN_SCYTHE_2_DAMAGE = BUILDER.comment("Damage for the second scythe swing").defineInRange("humanScythe2Damage", 25.0, 0.0, 200.0);
        HUMAN_SCYTHE_2_KNOCKBACK = BUILDER.comment("Knockback for the second scythe swing").defineInRange("humanScythe2Knockback", 6.0, 0.0, 20.0);

        HUMAN_SPEAR_MELEE_DAMAGE = BUILDER.comment("Damage for the melee spear attack").defineInRange("humanSpearMeleeDamage", 20.0, 0.0, 200.0);
        HUMAN_SPEAR_MELEE_KNOCKBACK = BUILDER.comment("Knockback for the melee spear attack").defineInRange("humanSpearMeleeKnockback", 8.0, 0.0, 20.0);

        HUMAN_SPEAR_MID_PUSH_DAMAGE = BUILDER.comment("Damage for the mid-range spear push attack").defineInRange("humanSpearMidPushDamage", 10.0, 0.0, 200.0);
        HUMAN_SPEAR_MID_PUSH_KNOCKBACK = BUILDER.comment("Knockback for the mid-range spear push attack").defineInRange("humanSpearMidPushKnockback", 8.0, 0.0, 20.0);
        HUMAN_SPEAR_MID_PULL_DAMAGE = BUILDER.comment("Damage for the mid-range spear pull attack").defineInRange("humanSpearMidPullDamage", 20.0, 0.0, 200.0);
        HUMAN_SPEAR_MID_PULL_KNOCKBACK = BUILDER.comment("Pull strength for the mid-range spear pull attack").defineInRange("humanSpearMidPullKnockback", 5.0, 0.0, 20.0);

        HUMAN_CHARGE_DAMAGE = BUILDER.comment("Damage dealt to players in the path of the charge").defineInRange("humanChargeDamage", 30.0, 0.0, 200.0);
        HUMAN_CHARGE_KNOCKBACK = BUILDER.comment("Knockback for the charge attack").defineInRange("humanChargeKnockback", 1.5, 0.0, 10.0);

        HUMAN_SPLASH_DAMAGE = BUILDER.comment("Damage for the scythe splash projectile").defineInRange("humanSplashDamage", 20.0, 0.0, 200.0);

        HUMAN_SPIKE_DAMAGE = BUILDER.comment("Damage for the ground spikes").defineInRange("humanSpikeDamage", 10.0, 0.0, 200.0);
        HUMAN_SPIKE_WITHER_DURATION = BUILDER.comment("Wither effect duration (in ticks) for ground spikes. 100 = 5 seconds. 0 = disable.").defineInRange("humanSpikeWitherDuration", 100, 0, 1200);

        HUMAN_MELEE_ATTACK_RADIUS = BUILDER.comment("Maximum distance for melee attacks (blocks)").defineInRange("humanMeleeAttackRadius", 3.0, 1.0, 50.0);
        HUMAN_MID_ATTACK_RADIUS_MAX = BUILDER.comment("Maximum distance for mid-range attacks (blocks)").defineInRange("humanMidAttackRadiusMax", 8.0, 1.0, 50.0);
        HUMAN_LONG_ATTACK_RADIUS_MIN = BUILDER.comment("Minimum distance for long-range attacks (blocks)").defineInRange("humanLongAttackRadiusMin", 6.0, 1.0, 50.0);
        HUMAN_MOVEMENT_STOP_RADIUS = BUILDER.comment("Distance at which boss stops walking and starts attacking (blocks)").defineInRange("humanMovementStopRadius", 8.0, 1.0, 50.0);

        HUMAN_PROJECTILE_SPIKE_DAMAGE = BUILDER.comment("Damage for the flying spike projectiles").defineInRange("humanProjectileSpikeDamage", 10.0, 0.0, 200.0);
        HUMAN_PROJECTILE_SPIKE_WITHER_DURATION = BUILDER.comment("Wither effect duration (in ticks) for flying spike projectiles. 100 = 5 sec.").defineInRange("humanProjectileSpikeWitherDuration", 100, 0, 1200);
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