package com.acme.shop.model;

public class Order {
    private long id;
    private String customer;
    private String item;
    private int amountCents;

    public Order() {
    }

    public Order(long id, String customer, String item, int amountCents) {
        this.id = id;
        this.customer = customer;
        this.item = item;
        this.amountCents = amountCents;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
}
