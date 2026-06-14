package com.minecraftcities.core.history;

public record TransactionEntry(
    long timestamp,
    String counterpartyName,
    long amount,
    boolean sent   // true = we sent Gold, false = we received Gold
) {}
