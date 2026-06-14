package com.minecraftcities.core.currency;

public enum Currency {
    GOLD,
    CITY,
    PREMIUM;

    public String translationKey() {
        return "minecraftcitiescore.currency." + name().toLowerCase();
    }
}
