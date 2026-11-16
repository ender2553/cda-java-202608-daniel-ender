package com.acme.orders;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AdminOrderRepository {

    private final Map<String, String> orders = new HashMap<>();

    public String findStatus(String orderId) {
        return orders.getOrDefault(orderId, "UNKNOWN");
    }

    public void deleteOrder(String orderId) {
        orders.remove(orderId);
    }

    public void overrideStatus(String orderId, String status) {
        orders.put(orderId, status);
    }

    public void refund(String orderId) {
        orders.put(orderId, "REFUNDED");
    }
}
