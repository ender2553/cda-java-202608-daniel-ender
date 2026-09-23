package com.cyberdev.pos.day2;

import java.util.Objects;

public final class Merchant {
    private final String merchantId;
    private final String displayName;

    public Merchant(String merchantId, String displayName) {
        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("merchantId must not be blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        this.merchantId = merchantId;
        this.displayName = displayName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getDisplayName() {
        return displayName;
    }

    // NOTE [POS2-9]: Merchant is an ENTITY type -- it is equal by IDENTITY
    // (merchantId) alone, not by all fields. Two Merchant instances with the same
    // merchantId but a different displayName still represent "the same merchant" (e.g. one
    // is a stale copy before a rename was applied); collapsing them by full-field equality
    // would make a HashSet<Merchant> treat a rename as "a different merchant showed up",
    // which is wrong. Common mistake: forgetting hashCode() after adding equals() -- a
    // Merchant that "looks correct" in equals() but keeps the default Object.hashCode()
    // will never be found again in a HashSet/HashMap keyed by it, because it lands in the
    // wrong bucket.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Merchant)) return false;
        Merchant other = (Merchant) o;
        return merchantId.equals(other.merchantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchantId);
    }
}
