package com.interviewai.service;

import com.interviewai.dao.OnboardingDAO;
import com.interviewai.model.OnboardingData;
import com.interviewai.model.User;
import com.interviewai.util.SessionContext;

import java.sql.SQLException;

/**
 * Handles multi-step onboarding: interview type, language, timeline, and context.
 * Persists onboarding data and triggers AI course generation after completion.
 */
public class OnboardingService {
    
    private final OnboardingDAO onboardingDAO = new OnboardingDAO();
    private MultiStageAIService multiStageAIService;

    public OnboardingService() {
        try {
            this.multiStageAIService = new MultiStageAIService();
            System.out.println("✓ Multi-Stage AI Service initialized");
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Failed to initialize Multi-Stage AI Service: " + e.getMessage());
        }
    }

    /**
     * Saves user onboarding preferences to DB and triggers multi-stage AI course generation asynchronously.
     * Calls the callback when complete with progress updates.
     */
    public void saveOnboardingDataAsync(OnboardingData data, 
                                       MultiStageAIService.ProgressCallback progressCallback,
                                       MultiStageAIService.CompletionCallback completionCallback) {
        System.out.println("Saving onboarding data: " + data);

        // Persist to DB (or update if exists)
        try {
            boolean saved = onboardingDAO.saveOnboardingData(data);
            System.out.println(saved ? "✓ Onboarding data saved" : "⚠️ Onboarding data not saved");
        } catch (SQLException e) {
            System.err.println("✗ Failed to save onboarding data: " + e.getMessage());
        }

        // Cache in session for quick access during this run
        SessionContext.setOnboardingData(data);
        
        if (multiStageAIService != null) {
            multiStageAIService.generateCourseAsync(data, progressCallback, completionCallback);
        } else {
            System.out.println("ℹ️  Multi-Stage AI service not available. Skipping course generation.");
            if (completionCallback != null) {
                completionCallback.onComplete(false, "AI Service not initialized");
            }
        }
    }
    
    /**
     * Retrieves saved onboarding data for a user.
     */
    public OnboardingData getOnboardingData(int userId) {
        try {
            return onboardingDAO.getByUserId(userId);
        } catch (SQLException e) {
            System.err.println("Failed to load onboarding data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if user has completed onboarding.
     */
    public boolean hasCompletedOnboarding(int userId) {
        try {
            return onboardingDAO.hasCompletedOnboarding(userId);
        } catch (SQLException e) {
            System.err.println("Failed to check onboarding completion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Initializes onboarding state for a new user.
     */
    public void startOnboarding(User user) {
        // Placeholder for any future initialization steps
        System.out.println("Starting onboarding for user: " + user.getUsername());
    }
}
