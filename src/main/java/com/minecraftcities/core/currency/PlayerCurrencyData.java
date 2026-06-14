package com.minecraftcities.core.currency;

import com.minecraftcities.core.history.TransactionEntry;
import com.minecraftcities.core.history.TransactionHistory;

public class PlayerCurrencyData {

    private long gold = 0;
    private long cityTokens = 0;
    private long premiumTokens = 0;
    private boolean starterGranted = false;
    private long earnRatePerMinute = 0;
    private final TransactionHistory history = new TransactionHistory();

    public boolean isStarterGranted()              { return starterGranted; }
    public void markStarterGranted()               { starterGranted = true; }
    public long getEarnRatePerMinute()             { return earnRatePerMinute; }
    public void setEarnRatePerMinute(long rate)    { earnRatePerMinute = Math.max(0, rate); }

    public long get(Currency currency) {
        return switch (currency) {
            case GOLD -> gold;
            case CITY -> cityTokens;
            case PREMIUM -> premiumTokens;
        };
    }

    public void set(Currency currency, long value) {
        switch (currency) {
            case GOLD -> gold = Math.max(0, value);
            case CITY -> cityTokens = Math.max(0, value);
            case PREMIUM -> premiumTokens = Math.max(0, value);
        }
    }

    public TransactionHistory history() {
        return history;
    }

    public void recordTransaction(TransactionEntry entry) {
        history.add(entry);
    }
}
