package com.minecraftcities.core.currency;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class CurrencyChangedEvent extends Event {

    private final ServerPlayer player;
    private final Currency currency;
    private final long oldValue;
    private final long newValue;

    public CurrencyChangedEvent(ServerPlayer player, Currency currency, long oldValue, long newValue) {
        this.player = player;
        this.currency = currency;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public ServerPlayer getPlayer() { return player; }
    public Currency getCurrency() { return currency; }
    public long getOldValue() { return oldValue; }
    public long getNewValue() { return newValue; }
}
