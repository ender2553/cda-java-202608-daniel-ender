package com.rti.legacy.accounts;

/**
 * A customer account. Balance is stored in whole cents.
 */
public class Account {

    public String id;
    public String owner;
    public long balance;

    public Account() {
    }

    public Account(String id, String owner, long balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }
}
