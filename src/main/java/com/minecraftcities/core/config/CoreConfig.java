package com.minecraftcities.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CoreConfig {

    public static final ModConfigSpec SPEC;

    // Premium currency
    public static final ModConfigSpec.BooleanValue PREMIUM_ENABLED;

    // City token cost formula
    public static final ModConfigSpec.EnumValue<CostFormula> CITY_COST_FORMULA;
    public static final ModConfigSpec.LongValue CITY_COST_BASE;

    // Gold passive earn - global toggle
    public static final ModConfigSpec.BooleanValue EARN_ENABLED;
    public static final ModConfigSpec.LongValue EARN_TICK_INTERVAL_SECONDS;

    // Mining earn
    public static final ModConfigSpec.BooleanValue EARN_MINING_ENABLED;
    public static final ModConfigSpec.LongValue EARN_MINING_GOLD_PER_BLOCK;

    // Mob kill earn
    public static final ModConfigSpec.BooleanValue EARN_MOB_KILL_ENABLED;
    public static final ModConfigSpec.LongValue EARN_MOB_KILL_GOLD_PER_KILL;

    // Walking earn
    public static final ModConfigSpec.BooleanValue EARN_WALKING_ENABLED;
    public static final ModConfigSpec.LongValue EARN_WALKING_GOLD_PER_100_BLOCKS;

    // Starter pack
    public static final ModConfigSpec.BooleanValue STARTER_CITY_TOKEN_ENABLED;
    public static final ModConfigSpec.LongValue STARTER_CITY_TOKEN_AMOUNT;

    // Admin permission level
    public static final ModConfigSpec.IntValue ADMIN_PERMISSION_LEVEL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("premium");
        PREMIUM_ENABLED = builder.comment("If false, Premium currency is hidden in all UIs and disabled in commands.")
            .define("enabled", true);
        builder.pop();

        builder.push("city_tokens");
        CITY_COST_FORMULA = builder.comment("Cost formula for founding cities. Options: CONSISTENT, EXPONENTIAL, FIBONACCI")
            .defineEnum("cost_formula", CostFormula.FIBONACCI);
        CITY_COST_BASE = builder.comment("Base cost (City Tokens) for the first city.")
            .defineInRange("cost_base", 1L, 1L, Long.MAX_VALUE);
        builder.pop();

        builder.push("gold");
        builder.push("passive_earn");
        EARN_ENABLED = builder.comment("Master toggle for passive Gold earn.")
            .define("enabled", true);
        EARN_TICK_INTERVAL_SECONDS = builder.comment("How often (in seconds) passive Gold is awarded.")
            .defineInRange("tick_interval_seconds", 60L, 1L, 3600L);

        builder.push("mining");
        EARN_MINING_ENABLED = builder.define("enabled", true);
        EARN_MINING_GOLD_PER_BLOCK = builder.comment("Gold awarded per block mined.")
            .defineInRange("gold_per_block", 1L, 0L, Long.MAX_VALUE);
        builder.pop();

        builder.push("mob_kills");
        EARN_MOB_KILL_ENABLED = builder.define("enabled", true);
        EARN_MOB_KILL_GOLD_PER_KILL = builder.comment("Gold awarded per mob killed.")
            .defineInRange("gold_per_kill", 5L, 0L, Long.MAX_VALUE);
        builder.pop();

        builder.push("walking");
        EARN_WALKING_ENABLED = builder.define("enabled", false);
        EARN_WALKING_GOLD_PER_100_BLOCKS = builder.comment("Gold awarded per 100 blocks walked.")
            .defineInRange("gold_per_100_blocks", 1L, 0L, Long.MAX_VALUE);
        builder.pop();

        builder.pop(); // passive_earn
        builder.pop(); // gold

        builder.push("starter");
        STARTER_CITY_TOKEN_ENABLED = builder.comment("If true, new players receive City Tokens on first join.")
            .define("enabled", true);
        STARTER_CITY_TOKEN_AMOUNT = builder.comment("How many City Tokens new players receive on first join.")
            .defineInRange("city_token_amount", 1L, 0L, Long.MAX_VALUE);
        builder.pop();

        builder.push("permissions");
        ADMIN_PERMISSION_LEVEL = builder.comment("Vanilla permission level required to use /currency admin commands.")
            .defineInRange("admin_permission_level", 2, 0, 4);
        builder.pop();

        SPEC = builder.build();
    }

    public enum CostFormula {
        CONSISTENT, EXPONENTIAL, FIBONACCI;

        /** Returns the City Token cost for a player founding their Nth city (1-indexed). */
        public long computeCost(long base, int citiesAlreadyFounded) {
            int n = citiesAlreadyFounded + 1;
            return switch (this) {
                case CONSISTENT -> base;
                case EXPONENTIAL -> base * (1L << (n - 1)); // base * 2^(n-1)
                case FIBONACCI -> {
                    long a = base, b = base;
                    for (int i = 2; i < n; i++) { long tmp = a + b; a = b; b = tmp; }
                    yield n == 1 ? base : b;
                }
            };
        }
    }
}
