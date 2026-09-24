package com.example.paintcalc.service;

import com.example.paintcalc.domain.*;
import org.springframework.stereotype.Service;

@Service
public final class ColorAdvisor {
    public ColorAdvice advise(PaintColor color, boolean smallRoom, boolean lowNaturalLight) {
        if (smallRoom && color == PaintColor.SUNSET_YELLOW) return new ColorCaution("Bright yellow may feel intense in a small room; consider one accent wall.");
        if (lowNaturalLight && color == PaintColor.FOREST_GREEN) return new ColorCaution("Dark green can reduce the feeling of light; test a sample first.");
        return new ColorRecommendation(color, color.advice());
    }
}
