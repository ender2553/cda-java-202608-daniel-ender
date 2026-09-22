package com.cyberdev.pos.day1;

import com.cyberdev.pos.exception.ValidationException;
import com.cyberdev.pos.testkit.GradedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class Day1Tests {

    private Product product(String id, String name, String price) {
        return new Product(id, name, new BigDecimal(price));
    }

    @Test

    @GradedTest(tag = "POS1-1", points = 3, description = "LineItem rejects non-positive quantity with ValidationException")
    public void lineItem_rejectsZeroQuantity() {
        Product p = product("P1", "Widget", "9.99");
        assertThrows(ValidationException.class, () -> new LineItem(p, 0), "quantity 0 must be rejected as a ValidationException");
    }

    @Test

    @GradedTest(tag = "POS1-1", points = 3, description = "LineItem rejects negative quantity with ValidationException")
    public void lineItem_rejectsNegativeQuantity() {
        Product p = product("P1", "Widget", "9.99");
        assertThrows(ValidationException.class, () -> new LineItem(p, -3), "negative quantity must be rejected as a ValidationException");
    }

    @Test

    @GradedTest(tag = "POS1-1", points = 3, description = "LineItem computes subtotal correctly")
    public void lineItem_computesSubtotal() {
        Product p = product("P1", "Widget", "9.99");
        LineItem item = new LineItem(p, 3);
        assertEquals(new BigDecimal("29.97"), item.getSubtotal(), "subtotal should be price * quantity");
    }

    @Test

    @GradedTest(tag = "POS1-2", points = 5, description = "Cart.addItem stores items and computes total")
    public void cart_addItem_and_total() {
        Cart cart = new Cart();
        cart.addItem(new LineItem(product("P1", "Widget", "10.00"), 2));
        cart.addItem(new LineItem(product("P2", "Gadget", "5.50"), 1));
        assertEquals(new BigDecimal("25.50"), cart.getTotal(), "cart total should sum subtotals");
    }

    @Test

    @GradedTest(tag = "POS1-3", points = 9, description = "Cart.getItems does not leak mutable state (mutation attempt on returned list)")
    public void cart_getItems_isImmutableView() {
        Cart cart = new Cart();
        cart.addItem(new LineItem(product("P1", "Widget", "10.00"), 1));
        List<LineItem> viewed = cart.getItems();
        assertThrows(UnsupportedOperationException.class, () -> viewed.add(new LineItem(product("P2", "Gadget", "1.00"), 1)),
                "returned list must not be mutable, or must be a copy not backing the cart");
    }

    @Test

    @GradedTest(tag = "POS1-3", points = 5, description = "Cart.getItems copy/view does not affect the real cart even if caller tries a workaround")
    public void cart_getItems_copyDoesNotAliasBackingList() {
        Cart cart = new Cart();
        cart.addItem(new LineItem(product("P1", "Widget", "10.00"), 1));
        List<LineItem> viewed = new ArrayList<>(cart.getItems());
        viewed.clear();
        assertEquals(1, cart.getItems().size(), "clearing a copy of the returned items must not affect the cart");
    }

    @Test

    @GradedTest(tag = "POS1-4", points = 3, description = "CardBrand.fromNumber recognizes VISA")
    public void cardBrand_visa() {
        assertEquals(CardBrand.VISA, CardBrand.fromNumber("4111111111111111"));
    }

    @Test

    @GradedTest(tag = "POS1-4", points = 3, description = "CardBrand.fromNumber recognizes MASTERCARD")
    public void cardBrand_mastercard() {
        assertEquals(CardBrand.MASTERCARD, CardBrand.fromNumber("5500000000000004"));
    }

    @Test

    @GradedTest(tag = "POS1-4", points = 3, description = "CardBrand.fromNumber recognizes AMEX (34 and 37 prefixes)")
    public void cardBrand_amex() {
        assertEquals(CardBrand.AMEX, CardBrand.fromNumber("340000000000009"));
        assertEquals(CardBrand.AMEX, CardBrand.fromNumber("370000000000002"));
    }

    @Test

    @GradedTest(tag = "POS1-4", points = 3, description = "CardBrand.fromNumber recognizes DISCOVER (6011 prefix)")
    public void cardBrand_discover() {
        assertEquals(CardBrand.DISCOVER, CardBrand.fromNumber("6011000000000004"));
    }

    @Test

    @GradedTest(tag = "POS1-4", points = 3, description = "CardBrand.fromNumber fails closed to UNKNOWN, never throws")
    public void cardBrand_unknownFailsClosed() {
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromNumber("9999999999999999"));
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromNumber("not-a-card"));
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromNumber(null));
        assertEquals(CardBrand.UNKNOWN, CardBrand.fromNumber(""));
    }

    @Test

    @GradedTest(tag = "POS1-5", points = 4, description = "PaymentMethod (via CreditCard) rejects malformed last4 with ValidationException")
    public void paymentMethod_rejectsBadLast4() {
        assertThrows(ValidationException.class,
                () -> new CreditCard("Alice", "12a4", CardBrand.VISA, 12, 2030),
                "non-digit last4 must be rejected as a ValidationException");
        assertThrows(ValidationException.class,
                () -> new CreditCard("Alice", "123", CardBrand.VISA, 12, 2030),
                "short last4 must be rejected as a ValidationException");
    }

    @Test

    @GradedTest(tag = "POS1-5", points = 3, description = "PaymentMethod rejects blank cardholder name with ValidationException")
    public void paymentMethod_rejectsBlankName() {
        assertThrows(ValidationException.class,
                () -> new CreditCard("  ", "1234", CardBrand.VISA, 12, 2030),
                "blank cardholder name must be rejected as a ValidationException");
    }

    @Test

    @GradedTest(tag = "POS1-6", points = 5, description = "CreditCard extends PaymentMethod and calls super correctly")
    public void creditCard_extendsAndDescribes() {
        CreditCard card = new CreditCard("Bob", "4242", CardBrand.VISA, 6, 2031);
        assertTrue(card instanceof PaymentMethod, "CreditCard must extend PaymentMethod");
        assertTrue(card.describe().contains("4242"), "describe() should reference last4");
        assertEquals("Bob", card.getCardholderName(), "super(...) must set cardholderName");
    }

    @Test

    @GradedTest(tag = "POS1-6", points = 4, description = "CreditCard implements Refundable with fail-closed amount check")
    public void creditCard_refund() {
        CreditCard card = new CreditCard("Bob", "4242", CardBrand.VISA, 6, 2031);
        assertTrue(card instanceof Refundable, "CreditCard must implement Refundable");
        assertTrue(card.refund(new BigDecimal("10.00")), "valid positive amount should succeed");
        assertFalse(card.refund(BigDecimal.ZERO), "zero amount must fail closed");
        assertFalse(card.refund(new BigDecimal("-5.00")), "negative amount must fail closed");
    }

    @Test

    @GradedTest(tag = "POS1-7", points = 4, description = "GiftCard extends PaymentMethod, calls super correctly")
    public void giftCard_extendsAndDescribes() {
        GiftCard card = new GiftCard("Carol", "9876", new BigDecimal("25.00"));
        assertTrue(card instanceof PaymentMethod, "GiftCard must extend PaymentMethod");
        assertEquals("Carol", card.getCardholderName());
        assertTrue(card.describe().contains("9876"));
    }

    @Test

    @GradedTest(tag = "POS1-7", points = 4, description = "GiftCard does NOT implement Refundable (interface segregation)")
    public void giftCard_notRefundable() {
        GiftCard card = new GiftCard("Carol", "9876", new BigDecimal("25.00"));
        assertFalse(Refundable.class.isAssignableFrom(card.getClass()), "GiftCard must not implement Refundable");
    }

    @Test

    @GradedTest(tag = "POS1-8", points = 4, description = "TerminalRegister rejects duplicate transaction ids (fail-closed)")
    public void terminal_rejectsDuplicateTransactionId() {
        TerminalRegister register = new TerminalRegister();
        assertTrue(register.recordTransactionId("TXN-1"), "first time recording should succeed");
        assertFalse(register.recordTransactionId("TXN-1"), "duplicate id must be rejected");
    }

    @Test

    @GradedTest(tag = "POS1-10", points = 4, description = "PaymentMethod equals()/hashCode() by cardholderName+last4, consistent across subclasses")
    public void paymentMethod_equalsAndHashCodeAcrossSubclasses() {
        CreditCard cardA = new CreditCard("Erin", "4242", CardBrand.VISA, 6, 2031);
        CreditCard cardB = new CreditCard("Erin", "4242", CardBrand.MASTERCARD, 1, 2029);
        GiftCard giftSameIdentity = new GiftCard("Erin", "4242", new BigDecimal("10.00"));
        CreditCard differentHolder = new CreditCard("Frank", "4242", CardBrand.VISA, 6, 2031);

        assertTrue(cardA.equals(cardB), "two PaymentMethods with the same cardholderName+last4 must be equal regardless of other fields");
        assertEquals(cardA.hashCode(), cardB.hashCode(), "equal PaymentMethods must have equal hashCodes");
        assertTrue(cardA.equals(giftSameIdentity), "equality is by cardholderName+last4 across the hierarchy, not by concrete subclass");
        assertFalse(cardA.equals(differentHolder), "different cardholderName must not be equal");

        Set<PaymentMethod> set = new HashSet<>();
        set.add(cardA);
        set.add(cardB);
        assertEquals(1, set.size(), "a HashSet<PaymentMethod> must collapse equal-by-value instances to one entry");
    }

    @Test

    @GradedTest(tag = "POS1-9", points = 5, description = "TerminalRegister settlement queue is FIFO and drains to null")
    public void terminal_settlementQueueFifo() {
        TerminalRegister register = new TerminalRegister();
        Cart first = new Cart();
        first.addItem(new LineItem(product("P1", "First", "1.00"), 1));
        Cart second = new Cart();
        second.addItem(new LineItem(product("P2", "Second", "2.00"), 1));

        register.enqueueForSettlement(first);
        register.enqueueForSettlement(second);

        assertEquals(2, register.pendingSettlementCount());
        assertTrue(register.settleNext() == first, "first enqueued cart should settle first (FIFO)");
        assertTrue(register.settleNext() == second, "second enqueued cart should settle second");
        assertNull(register.settleNext(), "settleNext on empty queue should return null, not throw");
    }
}
