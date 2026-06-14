package com.minecraftcities.core.currency;

/**
 * Pure-Java currency operations on PlayerCurrencyData.
 * No Minecraft/NeoForge dependencies — fully unit-testable.
 * CurrencyManager wraps these for player-level use with event firing.
 */
public final class CurrencyOps {

    private CurrencyOps() {}

    public static long getBalance(PlayerCurrencyData data, Currency currency) {
        return data.get(currency);
    }

    public static void add(PlayerCurrencyData data, Currency currency, long amount) {
        data.set(currency, data.get(currency) + amount);
    }

    /**
     * Returns false (and makes no change) if the balance is insufficient.
     */
    public static boolean subtract(PlayerCurrencyData data, Currency currency, long amount) {
        long old = data.get(currency);
        if (old < amount) return false;
        data.set(currency, old - amount);
        return true;
    }

    public static void set(PlayerCurrencyData data, Currency currency, long value) {
        data.set(currency, value);
    }
}
