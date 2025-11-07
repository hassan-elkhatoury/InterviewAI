package com.interviewai.util;

import com.interviewai.model.OnboardingData;
import com.interviewai.model.User;

/**
 * Simple in-memory session context for current logged-in user.
 * TODO: Replace with persistent session if needed.
 */
public final class SessionContext {
    private static User currentUser;
    private static OnboardingData onboardingData;
    private static Integer activeCourseId;
    private static Integer activeChapterId;

    private SessionContext() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void setOnboardingData(OnboardingData data) {
        onboardingData = data;
    }
    
    public static OnboardingData getOnboardingData() {
        return onboardingData;
    }

    public static void setActiveCourseId(Integer courseId) {
        activeCourseId = courseId;
    }

    public static Integer getActiveCourseId() {
        return activeCourseId;
    }

    public static void setActiveChapterId(Integer chapterId) {
        activeChapterId = chapterId;
    }

    public static Integer getActiveChapterId() {
        return activeChapterId;
    }
}
