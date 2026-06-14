package com.minecraftcities.core.currency;

import com.minecraftcities.core.history.TransactionEntry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Player-facing currency operations. Delegates pure logic to CurrencyOps and fires
 * CurrencyChangedEvent on every mutation so downstream systems can react.
 */
public class CurrencyManager {

    private CurrencyManager() {}

    public static long getBalance(ServerPlayer player, Currency currency) {
        return CurrencyOps.getBalance(player.getData(CurrencyAttachments.CURRENCY_DATA.get()), currency);
    }

    public static void add(ServerPlayer player, Currency currency, long amount) {
        PlayerCurrencyData data = player.getData(CurrencyAttachments.CURRENCY_DATA.get());
        long old = data.get(currency);
        CurrencyOps.add(data, currency, amount);
        player.setData(CurrencyAttachments.CURRENCY_DATA.get(), data);
        NeoForge.EVENT_BUS.post(new CurrencyChangedEvent(player, currency, old, data.get(currency)));
    }

    public static boolean subtract(ServerPlayer player, Currency currency, long amount) {
        PlayerCurrencyData data = player.getData(CurrencyAttachments.CURRENCY_DATA.get());
        long old = data.get(currency);
        if (!CurrencyOps.subtract(data, currency, amount)) return false;
        player.setData(CurrencyAttachments.CURRENCY_DATA.get(), data);
        NeoForge.EVENT_BUS.post(new CurrencyChangedEvent(player, currency, old, data.get(currency)));
        return true;
    }

    public static void set(ServerPlayer player, Currency currency, long value) {
        PlayerCurrencyData data = player.getData(CurrencyAttachments.CURRENCY_DATA.get());
        long old = data.get(currency);
        CurrencyOps.set(data, currency, value);
        player.setData(CurrencyAttachments.CURRENCY_DATA.get(), data);
        NeoForge.EVENT_BUS.post(new CurrencyChangedEvent(player, currency, old, value));
    }

    /**
     * Transfers Gold from sender to receiver. Records transaction history on both.
     * Returns false if sender has insufficient Gold.
     */
    public static boolean transfer(ServerPlayer from, ServerPlayer to, long amount) {
        if (!subtract(from, Currency.GOLD, amount)) return false;
        add(to, Currency.GOLD, amount);
        long now = System.currentTimeMillis();
        from.getData(CurrencyAttachments.CURRENCY_DATA.get())
            .recordTransaction(new TransactionEntry(now, to.getName().getString(), amount, true));
        to.getData(CurrencyAttachments.CURRENCY_DATA.get())
            .recordTransaction(new TransactionEntry(now, from.getName().getString(), amount, false));
        return true;
    }
}
