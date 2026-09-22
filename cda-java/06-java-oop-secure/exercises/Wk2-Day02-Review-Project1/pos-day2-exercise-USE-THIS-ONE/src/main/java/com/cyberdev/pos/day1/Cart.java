package com.cyberdev.pos.day1;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A shopping cart. The whole point of this class is to teach students not to leak a
 * reference to mutable internal state, in either direction.
 */
public final class Cart {

    private final List<LineItem> items = new ArrayList<>();

    // INSTRUCTOR NOTE [POS1-2]: addItem must not store a caller-supplied collection by
    // reference anywhere (there's no caller-supplied list parameter here, but the pattern
    // generalizes -- if a future overload took a List<LineItem>, it would need
    // `new ArrayList<>(suppliedList)` before storing it). For the single-item overload,
    // the risk is different: some students try to "optimize" by exposing `items` directly
    // via a package-private getter, or they build a second addItem(List<LineItem>) overload
    // in their own testing and forget to copy it. Grading focuses on getItems() below, but
    // this method establishes the pattern: items only enter the cart through this one
    // controlled path.
    public void addItem(LineItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        items.add(item);
    }

    // INSTRUCTOR NOTE [POS1-3]: The classic aliasing bug is `return items;` -- that hands
    // the caller a live reference to the cart's backing list, so `cart.getItems().clear()`
    // silently empties the cart from outside. The fix is an unmodifiable *view* or a copy.
    // Common near-miss: returning `new ArrayList<>(items)` works for outside mutation but
    // some students think `Collections.unmodifiableList(items)` alone is enough forever --
    // it prevents outside mutation via the returned list, which is what we grade, but it's
    // worth mentioning in review that the view still reflects the live list if code elsewhere
    // in the class (not the caller) mutates `items` afterward. Either the copy or the
    // unmodifiable-view answer is accepted; what matters is the caller cannot mutate the cart.
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
