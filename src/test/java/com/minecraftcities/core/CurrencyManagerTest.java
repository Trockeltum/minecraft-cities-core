package com.minecraftcities.core;

import com.minecraftcities.core.currency.Currency;
import com.minecraftcities.core.currency.CurrencyOps;
import com.minecraftcities.core.currency.PlayerCurrencyData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyManagerTest {

    private PlayerCurrencyData data;

    @BeforeEach
    void setup() {
        data = new PlayerCurrencyData();
    }

    @Test
    void add_increasesBalance() {
        CurrencyOps.add(data, Currency.GOLD, 100L);
        assertEquals(100L, data.get(Currency.GOLD));
    }

    @Test
    void subtract_decreasesBalance_whenSufficient() {
        data.set(Currency.GOLD, 200L);
        boolean result = CurrencyOps.subtract(data, Currency.GOLD, 50L);
        assertTrue(result);
        assertEquals(150L, data.get(Currency.GOLD));
    }

    @Test
    void subtract_returnsFalse_whenInsufficient() {
        data.set(Currency.GOLD, 30L);
        boolean result = CurrencyOps.subtract(data, Currency.GOLD, 100L);
        assertFalse(result);
        assertEquals(30L, data.get(Currency.GOLD));
    }

    @Test
    void set_setsExactBalance() {
        CurrencyOps.set(data, Currency.CITY, 5L);
        assertEquals(5L, data.get(Currency.CITY));
    }

    @Test
    void getBalance_returnsCorrectValue() {
        data.set(Currency.PREMIUM, 999L);
        assertEquals(999L, CurrencyOps.getBalance(data, Currency.PREMIUM));
    }
}
