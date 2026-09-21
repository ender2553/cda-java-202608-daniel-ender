package com.cyberdev.pos.day1;

import com.cyberdev.pos.exception.ValidationException;

import java.math.BigDecimal;

/**
 * A single line of a shopping cart: one product at a given quantity.
 */
public final class LineItem {

    private final Product product;
    private final int quantity;

    // TODO [POS1-1]: Validate the constructor arguments.
    public LineItem(Product product, int quantity) {
        if (product == null) {
            throw new ValidationException("Product must not be null");
        }

        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than 0");
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

