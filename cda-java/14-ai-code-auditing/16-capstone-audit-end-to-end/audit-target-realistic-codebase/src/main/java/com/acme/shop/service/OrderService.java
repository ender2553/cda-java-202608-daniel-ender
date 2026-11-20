package com.acme.shop.service;

import com.acme.shop.model.Order;
import com.acme.shop.repo.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> search(String item, String customer) {
        return orderRepository.searchByItem(item, customer);
    }

    public Optional<Order> get(long id, String customer) {
        return orderRepository.findByIdForCustomer(id, customer);
    }

    public List<Order> ordersForCustomer(String customer) {
        return orderRepository.findByCustomer(customer);
    }
}