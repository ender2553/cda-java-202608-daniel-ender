package com.acme.orders;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CartHolder {

    private final List<String> items = new ArrayList<>();

    public void add(String sku) {
        items.add(sku);
    }

    public List<String> items() {
        return items;
    }

    public void clear() {
        items.clear();
    }
}
