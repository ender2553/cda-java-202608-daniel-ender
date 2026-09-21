package com.cyberdev.pos.day1;

import com.cyberdev.pos.exception.ValidationException;

import java.math.BigDecimal;

/**
 * A single line of a shopping cart: one product at a given quantity.
 */
public final class LineItem {

    private final Product product;
    private final int quantity;

    // TODO [POS1-1]: Validate the constructor arguments. `product` must not be null, and
    // `quantity` must be strictly greater than 0 (fail closed -- reject 0 and negative
    // quantities). Throw ValidationException (com.cyberdev.pos.exception), NOT a raw
    // IllegalArgumentException -- ValidationException IS-A PosException, the app-wide base
    // exception type, so callers only ever need to catch POS-specific exception types. Only
    // assign the fields once both checks pass.
    public LineItem(Product product, int quantity) {
        throw new UnsupportedOperationException("TODO [POS1-1]: validate product/quantity (throw ValidationException) and assign fields (fail closed on quantity <= 0)");
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return product.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
