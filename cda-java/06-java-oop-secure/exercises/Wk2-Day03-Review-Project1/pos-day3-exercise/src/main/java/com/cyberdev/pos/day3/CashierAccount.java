package com.cyberdev.pos.day3;

import com.cyberdev.pos.exception.ValidationException;

import java.util.Objects;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Stores a cashier's id plus a PBKDF2 salt+hash for their PIN. The raw PIN is never stored
 * anywhere -- only the salt and the derived hash.
 *
 * NOTE on equality: CashierAccount is modeled as an ENTITY type, equal by
 * IDENTITY (cashierId) alone -- exactly like Day 2's Merchant, and unlike Day 1's Product
 * (a value type, equal by all fields). This is a deliberate choice, not an oversight, and
 * is worth spelling out because it is easy to get backwards here specifically: a naive
 * "generated record equals()" over (cashierId, salt, pinHash) would look reasonable at a
 * glance, but it is WRONG for two independent reasons:
 *   1. Entity semantics: re-enrolling the same cashier (a PIN reset) changes salt/pinHash
 *      but must still refer to "the same cashier" for identity-keyed lookups/caches/sets --
 *      the same argument Merchant's INSTRUCTOR NOTE makes for a rename.
 *   2. A record's generated equals() would compare the salt/pinHash byte[] fields by
 *      Arrays-unaware Object.equals() (byte[] has no value equals() of its own; Java
 *      records compare array-typed components by reference, NOT Arrays.equals()), so two
 *      accounts holding equal-by-value but different-by-reference salt/hash arrays would
 *      never compare equal even when they should represent the same enrollment -- and
 *      conversely, secret material should never be casually compared/logged via a
 *      generated toString() that prints raw byte[] hashes, which a record would also give
 *      you by default. Both problems point the same direction: a plain class with
 *      hand-written identity-only equals()/hashCode() and no toString() that echoes the
 *      hash/salt is the right shape here, not a record.
 */
public final class CashierAccount {

    private final String cashierId;
    private final byte[] salt;
    private final byte[] pinHash;

    public CashierAccount(String cashierId, byte[] salt, byte[] pinHash) {
        if (cashierId == null || cashierId.isBlank()) {
            throw new ValidationException("cashierId must not be blank");
        }
        if (salt == null || pinHash == null) {
            throw new ValidationException("salt and pinHash must not be null");
        }
        this.cashierId = cashierId;
        this.salt = salt.clone();
        this.pinHash = pinHash.clone();
    }

    public String getCashierId() {
        return cashierId;
    }

    public byte[] getSalt() {
        return salt.clone();
    }

    public byte[] getPinHash() {
        return pinHash.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CashierAccount)) return false;
        CashierAccount other = (CashierAccount) o;
        return cashierId.equals(other.cashierId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cashierId);
    }

    @Override
    public String toString() {

        return "CashierAccount{cashierId='" + cashierId + "'}";
    }
}
