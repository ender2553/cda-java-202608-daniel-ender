package com.example.paintcalc;

import com.example.paintcalc.service.SecurityReviewExample;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/** These tests should fail until the intentionally insecure code is repaired. */
class SecurityReviewExampleTest {
    @Test void passwordIsHashedAndCanBeVerified() {
        String raw = "correct horse battery staple";
        String hash = SecurityReviewExample.hashPassword(raw);
        assertNotEquals(raw, hash);
        assertTrue(SecurityReviewExample.verifyPassword(raw, hash));
        assertFalse(SecurityReviewExample.verifyPassword("wrong password", hash));
    }
    @Test void queryUsesAParameterPlaceholder() { assertTrue(SecurityReviewExample.safeUserQuery().contains("?")); }
    @Test void inventoryCopyCannotBeMutated() { assertThrows(UnsupportedOperationException.class, () -> SecurityReviewExample.safeInventoryCopy(new ArrayList<>(java.util.List.of("Brush"))).clear()); }
    @Test void costUsesExactDecimalArithmetic() { assertEquals(0, new BigDecimal("77.00").compareTo(SecurityReviewExample.safeCost(new BigDecimal("38.50"), 2))); }
}
