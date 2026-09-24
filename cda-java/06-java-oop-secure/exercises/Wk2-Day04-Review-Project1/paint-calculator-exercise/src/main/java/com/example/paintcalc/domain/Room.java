package com.example.paintcalc.domain;

import java.math.BigDecimal;

/** Encapsulates room dimensions and the wall-area rule. */
public final class Room {
    private final BigDecimal length;
    private final BigDecimal width;
    private final BigDecimal height;

    public Room(BigDecimal length, BigDecimal width, BigDecimal height) {
        this.length = DomainValidation.positive(length, "length");
        this.width = DomainValidation.positive(width, "width");
        this.height = DomainValidation.positive(height, "height");
    }
    public BigDecimal length() { return length; }
    public BigDecimal width() { return width; }
    public BigDecimal height() { return height; }
    public BigDecimal wallSquareFeet() { return length.add(width).multiply(BigDecimal.valueOf(2)).multiply(height); }
}
