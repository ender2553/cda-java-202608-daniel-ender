package com.acme.orders;

public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public long priceCents(int quantity) {
        return quantity * 1999L;
    }
}
