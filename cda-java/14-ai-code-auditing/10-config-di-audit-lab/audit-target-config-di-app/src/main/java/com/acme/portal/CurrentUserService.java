package com.acme.portal;

import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
