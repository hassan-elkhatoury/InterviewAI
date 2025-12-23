package com.interviewai.service;

import com.interviewai.dao.UserDAO;
import com.interviewai.model.User;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Authenticates against the database.
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        try {
            return userDAO.validateCredentials(username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns DB user if exists.
     */
    public User getUser(String username) {
        try {
            return userDAO.getByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
