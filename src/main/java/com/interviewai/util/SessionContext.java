package com.interviewai.util;

import com.interviewai.model.User;

/**
 * Simple in-memory session context for current logged-in user.
 * TODO: Replace with persistent session if needed.
 */
public final class SessionContext {
    private static User currentUser;

    private SessionContext() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }
}
