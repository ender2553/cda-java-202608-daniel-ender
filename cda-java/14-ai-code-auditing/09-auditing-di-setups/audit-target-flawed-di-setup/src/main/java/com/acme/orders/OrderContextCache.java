package com.acme.orders;

import org.springframework.stereotype.Component;

@Component
public class OrderContextCache {

    private String customerId;

    public String resolveCustomer(String incomingCustomerId) {
        if (customerId == null) {
            customerId = incomingCustomerId;
        }
        return customerId;
    }

    public String getCustomerId() {
        return customerId;
    }
}
