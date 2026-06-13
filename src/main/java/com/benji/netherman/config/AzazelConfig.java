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

    static {
        BUILDER.push("Azazel Boss Configuration");

        MAX_HEALTH = BUILDER.comment("Maximum health of Azazel")
                .defineInRange("maxHealth", 800.0, 100.0, 10000.0);

        MOVEMENT_SPEED = BUILDER.comment("Movement speed of Azazel")
                .defineInRange("movementSpeed", 0.2, 0.05, 1.0);

        KNOCKBACK_RESISTANCE = BUILDER.comment("Knockback resistance (1.0 = completely immune)")
                .defineInRange("knockbackResistance", 1.0, 0.0, 1.0);

        BUILDER.pop();
        BUILDER.push("Azazel Attack Damage");

        LAUNCH_ATTACK_DAMAGE = BUILDER.comment("Damage dealt by the launch explosion attack")
                .defineInRange("launchAttackDamage", 8.0, 0.0, 100.0);

        PULL_ATTACK_DAMAGE = BUILDER.comment("Damage dealt when pulling players in")
                .defineInRange("pullAttackDamage", 5.0, 0.0, 100.0);

        WIND_ATTACK_DAMAGE = BUILDER.comment("Damage dealt by the wind knockback attack")
                .defineInRange("windAttackDamage", 5.0, 0.0, 100.0);

        WHEEL_ATTACK_DAMAGE = BUILDER.comment("Damage dealt per hit during the wheel dash attack")
                .defineInRange("wheelAttackDamage", 4.0, 0.0, 100.0);

        BUILDER.pop();
        BUILDER.push("Azazel Attack Frequencies");

        ATTACK_CHANCE = BUILDER.comment("Chance (1 in X ticks) for Azazel to perform an active attack. Lower = faster attacks.")
                .defineInRange("attackChance", 80, 10, 600);

        PASSIVE_SUMMON_CHANCE = BUILDER.comment("Chance (1 in X ticks) to spawn minions passively while idle.")
                .defineInRange("passiveSummonChance", 600, 100, 2400);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}