package com.example.paintcalc;

import com.example.paintcalc.domain.*;
import com.example.paintcalc.service.StandardPaintCalculator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaintCalculatorTest {
    @Test void roundsGallonsUpAndUsesBigDecimalForCost() {
        var result = new StandardPaintCalculator().calculate(1, new Room(new BigDecimal("10"), new BigDecimal("12"), new BigDecimal("8")), 2, PaintColor.OCEAN_BLUE,
                new PaintProduct("Test Paint", new BigDecimal("40.00"), new BigDecimal("350")));
        assertEquals(2, result.gallons());
        assertEquals(new BigDecimal("80.00"), result.cost());
    }
    @Test void rejectsInvalidRoomDimensions() { assertThrows(IllegalArgumentException.class, () -> new Room(new BigDecimal("0"), BigDecimal.TEN, BigDecimal.TEN)); }
}
