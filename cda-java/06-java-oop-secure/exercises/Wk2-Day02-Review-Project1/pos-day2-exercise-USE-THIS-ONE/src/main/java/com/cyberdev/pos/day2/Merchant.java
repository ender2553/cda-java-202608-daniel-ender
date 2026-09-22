package com.cyberdev.pos.day2;

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

    // TODO [POS2-9]: Override equals()/hashCode() for Merchant. Merchant is an ENTITY type:
    // equality is by merchantId IDENTITY alone -- do NOT compare displayName (two Merchant
    // instances with the same merchantId but a different displayName still represent "the
    // same merchant", e.g. one is a stale copy before a rename). Use
    // Objects.hash(merchantId) for hashCode(). Remember: overriding equals() WITHOUT
    // hashCode() silently breaks HashSet/HashMap lookups.
}
