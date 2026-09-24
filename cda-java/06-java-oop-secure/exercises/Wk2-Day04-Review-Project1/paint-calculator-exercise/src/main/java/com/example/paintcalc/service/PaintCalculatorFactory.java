package com.example.paintcalc.service;

import org.springframework.stereotype.Component;

@Component
public final class PaintCalculatorFactory {
    private final PaintCalculator standard;
    public PaintCalculatorFactory(PaintCalculator standard) { this.standard = standard; }
    public PaintCalculator forRoom() { return standard; }
}
