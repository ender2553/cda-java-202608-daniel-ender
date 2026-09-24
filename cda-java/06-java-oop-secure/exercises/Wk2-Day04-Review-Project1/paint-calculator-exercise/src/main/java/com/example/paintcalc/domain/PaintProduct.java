package com.example.paintcalc.domain;

import java.math.BigDecimal;

public record PaintProduct(String name, BigDecimal pricePerGallon, BigDecimal coveragePerGallon) {
    public PaintProduct {
        name = DomainValidation.requiredName(name, "product name", 40);
        pricePerGallon = DomainValidation.positive(pricePerGallon, "price per gallon");
        coveragePerGallon = DomainValidation.positive(coveragePerGallon, "coverage per gallon");
    }
}
