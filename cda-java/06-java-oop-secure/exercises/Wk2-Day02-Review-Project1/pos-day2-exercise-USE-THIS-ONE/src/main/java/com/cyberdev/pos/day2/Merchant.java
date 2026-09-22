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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Merchant)) {
            return false;
        }

        Merchant other = (Merchant) o;
        return merchantId.equals(other.merchantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchantId);
    }
}

