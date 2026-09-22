package com.cyberdev.pos.day1;

import com.cyberdev.pos.exception.ValidationException;

import java.math.BigDecimal;

/**
 * A single line of a shopping cart: one product at a given quantity.
 */
public final class LineItem {

    private final Product product;
    private final int quantity;

    // INSTRUCTOR NOTE [POS1-1]: This constructor is the fail-closed validation checkpoint.
    // The lesson: reject bad input at the boundary (constructor) rather than letting a
    // zero/negative quantity silently corrupt downstream totals. Common student mistake:
    // validating with `quantity <= 0` is correct, but some students write `quantity < 0`
    // (misses zero) or clamp/silently coerce the value instead of throwing -- clamping is
    // a fail-OPEN behavior and should not earn credit even if it happens to pass a loose test.
    // As of the custom-exception-hierarchy addition, this must throw ValidationException
    // (com.cyberdev.pos.exception), not a raw IllegalArgumentException -- ValidationException
    // IS-A PosException, the app-wide base type, so callers that only expect POS-specific
    // exceptions are not surprised by a bare java.lang exception type leaking through.
    public LineItem(Product product, int quantity) {
        if (product == null) {
            throw new ValidationException("product must not be null");
        }
        if (quantity <= 0) {
            throw new ValidationException("quantity must be greater than 0, was " + quantity);
        }
        this.product = product;
        this.quantity = quantity;
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
