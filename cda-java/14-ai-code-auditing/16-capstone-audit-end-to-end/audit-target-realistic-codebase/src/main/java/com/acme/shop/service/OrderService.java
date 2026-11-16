package com.acme.shop.service;

import com.acme.shop.model.Order;
import com.acme.shop.repo.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> search(String item) {
        return orderRepository.searchByItem(item);
    }

    public Order get(long id) {
        return orderRepository.findById(id);
    }

    public List<Order> ordersForCustomer(String customer) {
        return orderRepository.findByCustomer(customer);
    }
}
