package com.acme.orders;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckoutController {

    private final CartHolder cartHolder;
    private final OrderContextCache orderContextCache;
    private final PricingStrategy pricingStrategy;
    private final InternalLedgerService ledgerService;

    public CheckoutController(CartHolder cartHolder,
                              OrderContextCache orderContextCache,
                              PricingStrategy pricingStrategy,
                              InternalLedgerService ledgerService) {
        this.cartHolder = cartHolder;
        this.orderContextCache = orderContextCache;
        this.pricingStrategy = pricingStrategy;
        this.ledgerService = ledgerService;
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam String customerId, @RequestParam String sku) {
        cartHolder.add(sku);
        String customer = orderContextCache.resolveCustomer(customerId);
        long total = pricingStrategy.priceCents(cartHolder.items().size());
        ledgerService.postEntry(customer, total);
        return "charged " + total + " to " + customer;
    }
}
