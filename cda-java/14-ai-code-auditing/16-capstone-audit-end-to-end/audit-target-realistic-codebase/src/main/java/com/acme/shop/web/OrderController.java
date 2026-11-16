package com.acme.shop.web;

import com.acme.shop.model.Order;
import com.acme.shop.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/search")
    public List<Order> search(@RequestParam String item) {
        return orderService.search(item);
    }

    @GetMapping("/{id}")
    public Order getOne(@PathVariable long id) {
        return orderService.get(id);
    }

    @GetMapping("/error-demo")
    public Map<String, Object> errorDemo(@RequestParam String value) {
        try {
            int parsed = Integer.parseInt(value);
            return Map.of("parsed", parsed);
        } catch (NumberFormatException e) {
            return Map.of("error", e.toString(),
                    "stack", java.util.Arrays.toString(e.getStackTrace()));
        }
    }
}
