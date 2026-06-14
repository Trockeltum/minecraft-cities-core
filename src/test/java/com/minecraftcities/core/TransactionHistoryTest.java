package com.minecraftcities.core;

import com.minecraftcities.core.history.TransactionEntry;
import com.minecraftcities.core.history.TransactionHistory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransactionHistoryTest {

    @Test
    void addEntry_capsAt20() {
        TransactionHistory history = new TransactionHistory();
        for (int i = 0; i < 25; i++) {
            history.add(new TransactionEntry(i, "Player" + i, i * 10L, true));
        }
        assertEquals(20, history.entries().size());
    }

    @Test
    void addEntry_newestIsFirst() {
        TransactionHistory history = new TransactionHistory();
        history.add(new TransactionEntry(1L, "Alice", 50L, true));
        history.add(new TransactionEntry(2L, "Bob", 100L, false));
        assertEquals("Bob", history.entries().get(0).counterpartyName());
    }

    @Test
    void addEntry_oldestDroppedWhenFull() {
        TransactionHistory history = new TransactionHistory();
        for (int i = 0; i < 20; i++) {
            history.add(new TransactionEntry(i, "Player" + i, i * 10L, true));
        }
        history.add(new TransactionEntry(99L, "Last", 999L, true));
        boolean hasFirst = history.entries().stream()
            .anyMatch(e -> e.counterpartyName().equals("Player0"));
        assertFalse(hasFirst);
    }
}
