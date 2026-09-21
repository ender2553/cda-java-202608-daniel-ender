package com.cyberdev.pos.day1;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A shopping cart. The whole point of this class is to teach you not to leak a reference
 * to mutable internal state, in either direction.
 */
public final class Cart {

    private final List<LineItem> items = new ArrayList<>();

    // TODO [POS1-2]: Add the item to the cart's backing list. Reject a null item (fail
    // closed) before adding.
    public void addItem(LineItem item) {
        throw new UnsupportedOperationException("TODO [POS1-2]: validate and add item to the cart's backing list");
    }

    // TODO [POS1-3]: Return the cart's items WITHOUT leaking a live reference to the
    // backing list -- a caller must not be able to mutate the cart via the returned List.
    // Use an unmodifiable view (e.g. Collections.unmodifiableList) or a defensive copy.
    public List<LineItem> getItems() {
        throw new UnsupportedOperationException("TODO [POS1-3]: return an immutable view or copy of items, never the live backing list");
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
