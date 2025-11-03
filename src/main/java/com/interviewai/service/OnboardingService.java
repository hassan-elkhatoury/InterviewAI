package com.interviewai.service;

import com.interviewai.model.OnboardingData;
import com.interviewai.model.User;
import com.interviewai.ai.AIClient;
import com.interviewai.ai.AIRequestBuilder;
import com.interviewai.util.ConfigLoader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handles multi-step onboarding: interview type, language, timeline, and context.
 * Persists onboarding answers for personalized AI simulation.
 * Triggers AI course generation after onboarding completion.
 */
public class OnboardingService {
    
    private AIClient aiClient;
    private Gson gson = new Gson();

    /**
     * Initialize OnboardingService with AI configuration.
     * Loads API endpoint and key from config.properties.
     */
    public OnboardingService() {
        try {
            // Load from config.properties
            String apiUrl = ConfigLoader.get("ai.api.url");
            String apiKey = ConfigLoader.get("ai.api.key");
            
            // Fallback to defaults if not configured
            if (apiUrl == null || apiUrl.isEmpty()) {
                apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
            }
            
            if (apiKey != null && !apiKey.isEmpty()) {
                // Pass the base URL and key separately - AIClient will append ?key=
                this.aiClient = new AIClient(apiUrl, apiKey);
                System.out.println("✓ AI Client initialized with Gemini API");
            } else {
                System.err.println("⚠️  Warning: AI API key not configured. Set 'ai.api.key' in config.properties");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize AI client: " + e.getMessage());
        }
    }

    /**
     * Saves user onboarding preferences and triggers AI course generation.
     * Executes asynchronously to avoid blocking the UI.
     */
    public void saveOnboardingData(OnboardingData data) {
        System.out.println("Saving onboarding data: " + data);
        
        if (aiClient != null) {
            generateAICourse(data);
        } else {
            System.out.println("ℹ️  AI client not available. Skipping course generation.");
        }
    }
    
    /**
     * Generates an AI-powered interview prep course based on onboarding preferences.
     * Executes asynchronously and outputs results to System.out.
     */
    private void generateAICourse(OnboardingData data) {
        System.out.println("\n🤖 Starting AI course generation...");
        System.out.println("  Interview Type: " + data.getInterviewType());
        System.out.println("  Language: " + data.getLanguage());
        System.out.println("  Timeline: " + data.getTimeline());
        System.out.println("  Context: " + data.getContext());
        
        // Build the course request
        String requestBody = AIRequestBuilder.buildCourseRequest(
            data.getInterviewType(),
            data.getLanguage(),
            data.getTimeline(),
            data.getContext()
        );
        
        // Send request asynchronously
        aiClient.sendCourseRequest(requestBody)
            .thenAccept(response -> {
                // Output the response to console
                System.out.println("\n✅ AI Course Generated Successfully!\n");
                System.out.println("=" + "=".repeat(99));
                System.out.println("RAW AI RESPONSE:");
                System.out.println("=" + "=".repeat(99));
                System.out.println(response);
                System.out.println("=" + "=".repeat(99) + "\n");
                
                // Try to pretty-print if valid JSON
                try {
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    String prettyJson = gson.toJson(jsonResponse);
                    System.out.println("FORMATTED RESPONSE:");
                    System.out.println(prettyJson);
                    System.out.println("\n");
                } catch (Exception e) {
                    System.out.println("(Note: Response is not valid JSON, displaying raw)");
                }
            })
            .exceptionally(ex -> {
                System.err.println("\n❌ AI Course Generation Failed:");
                System.err.println("   Error: " + ex.getMessage());
                return null;
            });
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
