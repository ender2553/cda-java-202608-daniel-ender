package com.acme.shop.web;

import com.acme.shop.model.Order;
import com.acme.shop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public List<Order> search(
            @RequestParam String item,
            Principal principal) {

        return orderService.search(item, principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(
            @PathVariable long id,
            Principal principal) {

        return ResponseEntity.of(
                orderService.get(id, principal.getName())
        );
    }

    @GetMapping("/error-demo")
    public Map<String, Object> errorDemo(
            @RequestParam String value) {

        try {
            int parsed = Integer.parseInt(value);
            return Map.of("parsed", parsed);

        } catch (NumberFormatException e) {
            return Map.of("error", "Invalid numeric value");
        }
    }
}