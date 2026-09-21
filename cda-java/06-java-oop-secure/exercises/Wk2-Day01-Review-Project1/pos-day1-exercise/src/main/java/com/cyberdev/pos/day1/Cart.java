package com.cyberdev.pos.day1;

import com.cyberdev.pos.exception.ValidationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A shopping cart. The whole point of this class is to teach you not to leak a reference
 * to mutable internal state, in either direction.
 */
public final class Cart {

    private final List<LineItem> items = new ArrayList<>();

    // TODO [POS1-2]: Add the item to the cart's backing list.
    public void addItem(LineItem item) {
        if (item == null) {
            throw new ValidationException("Line item must not be null");
        }

        items.add(item);
    }

    // TODO [POS1-3]: Return the cart's items WITHOUT leaking a live reference.
    public List<LineItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

