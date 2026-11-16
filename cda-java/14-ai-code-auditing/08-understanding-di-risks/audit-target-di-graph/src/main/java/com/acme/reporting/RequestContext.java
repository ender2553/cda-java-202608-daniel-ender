package com.acme.reporting;

import org.springframework.stereotype.Component;

@Component
public class RequestContext {

    private String currentUser;

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
