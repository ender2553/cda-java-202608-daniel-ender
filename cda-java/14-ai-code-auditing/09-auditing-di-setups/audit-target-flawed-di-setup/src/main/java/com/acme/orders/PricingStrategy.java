package com.acme.orders;

public interface PricingStrategy {
    long priceCents(int quantity);
}
