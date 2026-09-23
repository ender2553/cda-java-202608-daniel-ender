package com.cyberdev.pos.day1;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An immutable catalog item. Deliberately a plain class (not a record) so students see
 * manual immutability (final fields, no setters, defensive equals/hashCode) before Day 3
 * introduces records for contrast.
 */
public final class Product {

    private final String id;
    private final String name;
    private final BigDecimal unitPrice;

    public Product(String id, String name, BigDecimal unitPrice) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Product unitPrice must be non-negative");
        }
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    // INSTRUCTOR NOTE: Product is equal by ALL fields (id, name, unitPrice), not just id.
    // Justification: Product is modeled as a pure VALUE type here (immutable, no identity
    // beyond its data), so two Product instances that happen to share an id but disagree on
    // name/price would indicate a data bug worth surfacing as "not equal", not something to
    // paper over by comparing id alone. (Contrast with Day 2's TransactionRecord/Merchant and
    // Day 4's CashierAccount, which ARE equal by a single identity field, because those model
    // ENTITY types with a stable identity that can legitimately have other fields change over
    // time.)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return id.equals(product.id) && name.equals(product.name) && unitPrice.equals(product.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, unitPrice);
    }

    @Override
    public String toString() {
        return "Product{id='" + id + "', name='" + name + "', unitPrice=" + unitPrice + "}";
    }
}
