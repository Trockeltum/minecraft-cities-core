package com.minecraftcities.core;

import com.minecraftcities.core.config.CoreConfig.CostFormula;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CostFormulaTest {

    @Test
    void consistent_alwaysReturnsBase() {
        assertEquals(5L, CostFormula.CONSISTENT.computeCost(5L, 0));
        assertEquals(5L, CostFormula.CONSISTENT.computeCost(5L, 3));
        assertEquals(5L, CostFormula.CONSISTENT.computeCost(5L, 9));
    }

    @Test
    void exponential_doublesEachTime() {
        assertEquals(1L, CostFormula.EXPONENTIAL.computeCost(1L, 0));  // city 1: base
        assertEquals(2L, CostFormula.EXPONENTIAL.computeCost(1L, 1));  // city 2: base*2
        assertEquals(4L, CostFormula.EXPONENTIAL.computeCost(1L, 2));  // city 3: base*4
        assertEquals(8L, CostFormula.EXPONENTIAL.computeCost(1L, 3));  // city 4: base*8
    }

    @Test
    void fibonacci_growsAsFibonacciSequence() {
        assertEquals(1L, CostFormula.FIBONACCI.computeCost(1L, 0));   // 1
        assertEquals(1L, CostFormula.FIBONACCI.computeCost(1L, 1));   // 1
        assertEquals(2L, CostFormula.FIBONACCI.computeCost(1L, 2));   // 2
        assertEquals(3L, CostFormula.FIBONACCI.computeCost(1L, 3));   // 3
        assertEquals(5L, CostFormula.FIBONACCI.computeCost(1L, 4));   // 5
        assertEquals(8L, CostFormula.FIBONACCI.computeCost(1L, 5));   // 8
    }
}
