package com.interviewai.service;

import java.util.HashMap;
import java.util.Map;

import com.interviewai.dao.UserDAO;
import com.interviewai.model.User;

public class AuthService {

    // Demo in-memory users (username -> password)
    private final Map<String, String> demoUsers = new HashMap<>();
    private final UserDAO userDAO = new UserDAO();

    public AuthService() {
        // Add some demo users
        demoUsers.put("admin", "password");
        demoUsers.put("user", "1234");
    }

    /**
     * Tries DB-backed auth first; falls back to demo users.
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        try {
            if (userDAO.validateCredentials(username, password)) return true;
        } catch (Exception ignored) { }
        return demoUsers.containsKey(username) && demoUsers.get(username).equals(password);
    }

    /**
     * Returns DB user if exists, otherwise a demo user object.
     */
    public User getDemoUser(String username) {
        try {
            User fromDb = userDAO.getByUsername(username);
            if (fromDb != null) return fromDb;
        } catch (Exception ignored) { }
        if (demoUsers.containsKey(username)) {
            User u = new User();
            u.setUsername(username);
            u.setRole(username.equals("admin") ? "ADMIN" : "CANDIDATE");
            return u;
        }
        return null;
    }
}
