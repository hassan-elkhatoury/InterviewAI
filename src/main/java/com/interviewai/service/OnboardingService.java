package com.interviewai.service;

import com.interviewai.model.OnboardingData;
import com.interviewai.model.User;

/**
 * Handles multi-step onboarding: interview type, language, timeline, and context.
 * Triggers AI course generation after onboarding completion.
 */
public class OnboardingService {
    
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
     * Saves user onboarding preferences and triggers multi-stage AI course generation asynchronously.
     * Calls the callback when complete with progress updates.
     */
    public void saveOnboardingDataAsync(OnboardingData data, 
                                       MultiStageAIService.ProgressCallback progressCallback,
                                       Runnable onComplete) {
        System.out.println("Saving onboarding data: " + data);
        
        if (multiStageAIService != null) {
            multiStageAIService.generateCourseAsync(data, progressCallback, onComplete);
        } else {
            System.out.println("ℹ️  Multi-Stage AI service not available. Skipping course generation.");
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    
    /**
     * Retrieves saved onboarding data for a user.
     * TODO: Load from database.
     */
    public OnboardingData getOnboardingData(int userId) {
        // TODO: Load from database
        return null;
    }

    

    /**
     * Checks if user has completed onboarding.
     */


    public boolean hasCompletedOnboarding(int userId) {
        // TODO: Check database
        return false;
    }



    /**
     * Initializes onboarding state for a new user.
     */
    public void startOnboarding(User user) {
        // TODO: initialize onboarding state for user
        System.out.println("Starting onboarding for user: " + user.getUsername());
    }
}
