package com.example.paintcalc.service;

import com.example.paintcalc.domain.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public final class StandardPaintCalculator implements PaintCalculator {
    public PaintEstimate calculate(long userId, Room room, int coats, PaintColor color, PaintProduct product) {
        if (coats < 1 || coats > 3) throw new IllegalArgumentException("coats must be 1-3");

        BigDecimal totalArea = room.wallSquareFeet()
                .multiply(BigDecimal.valueOf(coats));

        int gallons = totalArea
                .divide(product.coveragePerGallon(), 0, RoundingMode.CEILING)
                .intValue();

        BigDecimal cost = product.pricePerGallon()
                .multiply(BigDecimal.valueOf(gallons));

        return new PaintEstimate(
                userId,
                room.wallSquareFeet(),
                coats,
                color,
                gallons,
                cost
        );
    }
}
