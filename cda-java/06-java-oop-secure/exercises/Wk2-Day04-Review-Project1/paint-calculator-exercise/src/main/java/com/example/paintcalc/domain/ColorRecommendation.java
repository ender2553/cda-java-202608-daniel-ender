package com.example.paintcalc.domain;

public record ColorRecommendation(PaintColor color, String message) implements ColorAdvice { }
