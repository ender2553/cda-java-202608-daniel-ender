package com.acme.portal;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AdminUserRepository {

    private final Map<String, String> profiles = new HashMap<>();

    public String findProfile(String userId) {
        return profiles.getOrDefault(userId, "{}");
    }

    public void updateProfile(String userId, String profileJson) {
        profiles.put(userId, profileJson);
    }

    public void deleteUser(String userId) {
        profiles.remove(userId);
    }

    public void grantRole(String userId, String role) {
        profiles.put(userId, "{\"role\":\"" + role + "\"}");
    }
}
