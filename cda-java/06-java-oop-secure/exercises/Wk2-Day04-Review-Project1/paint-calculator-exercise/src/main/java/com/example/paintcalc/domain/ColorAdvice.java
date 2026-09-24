package com.example.paintcalc.domain;

public sealed interface ColorAdvice permits ColorRecommendation, ColorCaution {
    String message();
}
