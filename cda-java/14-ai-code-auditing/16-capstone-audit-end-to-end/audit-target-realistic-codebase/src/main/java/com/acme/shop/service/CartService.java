package com.acme.shop.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    // Items for the request currently being processed.
    private final List<String> currentItems = new ArrayList<>();

    public void addItem(String item) {
        currentItems.add(item);
    }

    public List<String> currentCart() {
        return currentItems;
    }

    public int total() {
        return currentItems.size();
    }

    public void checkout() {
        currentItems.clear();
    }
}
