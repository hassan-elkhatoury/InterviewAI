package com.interviewai.service;

import com.interviewai.model.OnboardingData;
import com.interviewai.model.User;

/**
 * Handles multi-step onboarding: interview type, language, timeline, and context.
 * Triggers AI course generation after onboarding completion.
 */
public class OnboardingService {
    
    private AIService aiService;

    public OnboardingService() {
        try {
            this.aiService = new AIService();
            System.out.println("✓ AI Service initialized");
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Failed to initialize AI Service: " + e.getMessage());
        }
    }

    /**
     * Saves user onboarding preferences and triggers AI course generation.
     */
    public void saveOnboardingData(OnboardingData data) {
        System.out.println("Saving onboarding data: " + data);
        
        if (aiService != null) {
            generateAICourse(data);
        } else {

            System.out.println("ℹ️  AI service not available. Skipping course generation.");
            
        }
    }
    
    /**
     * Generates an AI-powered interview prep course based on onboarding preferences.
     */
    private void generateAICourse(OnboardingData data) {
        System.out.println("\n🤖 Starting AI course generation...");
        System.out.println("  Interview Type: " + data.getInterviewType());
        System.out.println("  Language: " + data.getLanguage());
        System.out.println("  Timeline: " + data.getTimeline());
        System.out.println("  Context: " + data.getContext());
        
        try {
            // Build the prompt
            String prompt = aiService.buildCoursePrompt(
                data.getInterviewType(),
                data.getLanguage(),
                data.getTimeline(),
                data.getContext()
            );
            
            // Send request and get response
            String response = aiService.sendRequest(prompt);
            
            // Display response in terminal
            System.out.println("\n✅ AI Course Generated Successfully!\n");
            System.out.println("=" + "=".repeat(80));
            System.out.println("AI RESPONSE:");
            System.out.println("=" + "=".repeat(80));
            System.out.println(response);
            System.out.println("=" + "=".repeat(80) + "\n");

            // Parse and save course to database
            com.interviewai.model.GeneratedCourse course = com.interviewai.model.GeneratedCourse.fromJson(response, data.getUserId());
            new com.interviewai.dao.CourseDAO().saveGeneratedCourse(course);
            System.out.println("Course saved to database for user: " + data.getUserId());

        } catch (Exception e) {
            System.err.println("\n❌ AI Course Generation Failed:");
            System.err.println("   Error: " + e.getMessage());
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
