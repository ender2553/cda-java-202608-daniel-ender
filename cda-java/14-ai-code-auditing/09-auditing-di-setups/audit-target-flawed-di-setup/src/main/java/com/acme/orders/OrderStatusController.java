package com.acme.orders;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderStatusController {

    private final AdminOrderRepository orderRepository;

    public OrderStatusController(AdminOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/orders/{id}/status")
    public String status(@PathVariable String id) {
        return orderRepository.findStatus(id);
    }
}
