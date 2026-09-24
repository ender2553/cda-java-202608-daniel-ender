package com.example.paintcalc.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PaintEstimate(long userId, BigDecimal area, int coats, PaintColor color, int gallons, BigDecimal cost) {
    public PaintEstimate {
        if (userId <= 0 || coats < 1 || gallons < 1) throw new IllegalArgumentException("invalid estimate");
        if (area == null || area.signum() <= 0 || cost == null || cost.signum() < 0) throw new IllegalArgumentException("invalid estimate values");
        cost = cost.setScale(2, RoundingMode.HALF_UP);
    }
}
