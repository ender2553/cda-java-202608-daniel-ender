package com.example.rpg.domain;
public record UserAccount(long id,String username,String passwordHash) {
    public UserAccount { if(id<0) throw new IllegalArgumentException("id cannot be negative"); username=Domain.cleanName(username,"username",32); if(passwordHash==null||passwordHash.isBlank())throw new IllegalArgumentException("password hash required"); }
}
