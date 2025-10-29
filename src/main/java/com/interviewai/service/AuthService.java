package com.interviewai.service;

import com.interviewai.model.User;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    // Demo in-memory users (username -> password)
    private Map<String, String> demoUsers = new HashMap<>();

    public AuthService() {
        // Add some demo users
        demoUsers.put("admin", "password");
        demoUsers.put("user", "1234");
    }


    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        return demoUsers.containsKey(username) && demoUsers.get(username).equals(password);
    }


    public User getDemoUser(String username) {
        if (demoUsers.containsKey(username)) {
            User u = new User();
            u.setUsername(username);
            u.setRole(username.equals("admin") ? "ADMIN" : "CANDIDATE");
            return u;
        }
        return null;
    }
}
