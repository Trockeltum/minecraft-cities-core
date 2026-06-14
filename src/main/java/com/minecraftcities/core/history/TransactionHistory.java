package com.minecraftcities.core.history;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TransactionHistory {

    private static final int MAX_ENTRIES = 20;
    private final Deque<TransactionEntry> entries = new ArrayDeque<>();

    public void add(TransactionEntry entry) {
        entries.addFirst(entry);
        if (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
    }

    public List<TransactionEntry> entries() {
        return new ArrayList<>(entries);
    }

    public void clear() {
        entries.clear();
    }
}
