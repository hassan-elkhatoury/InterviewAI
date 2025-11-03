package com.interviewai.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Handles HTTP communication with AI APIs (OpenAI, etc.).
 * Provides asynchronous course generation requests.
 */
public class AIClient {
    
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    
    private final String apiUrl;
    private final String apiKey;
    
    /**
     * Initialize AI client with API endpoint and authentication.
     * 
     * @param apiUrl Full API endpoint URL (e.g., https://api.openai.com/v1/completions)
     * @param apiKey Bearer token for authentication
     */
    public AIClient(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }
    
    /**
     * Send a course generation request to the AI API asynchronously.
     * 
     * @param requestBody JSON request body (from AIRequestBuilder)
     * @return CompletableFuture containing the AI response as JSON string
     */
    public CompletableFuture<String> sendCourseRequest(String requestBody) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // For Gemini API, append API key to URL as query parameter
                String fullUrl = apiUrl + "?key=" + apiKey;
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(fullUrl))
                        .timeout(Duration.ofSeconds(60))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                
                System.out.println("=== AI API Request ===");
                System.out.println("URL: " + apiUrl);
                System.out.println("Request Body: " + requestBody);
                
                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                System.out.println("Response Status: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
                
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                } else {
                    throw new RuntimeException("API Error " + response.statusCode() + 
                            ": " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to call AI API: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Send a simple text completion request to the AI API.
     * Useful for feedback, hints, or follow-up questions.
     * 
     * @param prompt The prompt to send
     * @return CompletableFuture containing the response text
     */
    public CompletableFuture<String> sendPromptRequest(String prompt) {
        org.json.JSONObject json = new org.json.JSONObject();
        json.put("prompt", prompt);
        json.put("max_tokens", 500);
        json.put("temperature", 0.7);
        
        return sendCourseRequest(json.toString());
    }
    
    /**
     * Send a batch of practice questions generation request.
     * 
     * @param chapter Chapter name/topic
     * @param difficulty Difficulty level (EASY, MEDIUM, HARD)
     * @param count Number of questions to generate
     * @return CompletableFuture containing practice questions JSON
     */
    public CompletableFuture<String> generatePracticeQuestions(String chapter, 
                                                                String difficulty, 
                                                                int count) {
        String requestBody = AIRequestBuilder.buildPracticeQuestionsRequest(chapter, difficulty);
        return sendCourseRequest(requestBody);
    }
    
    /**
     * Health check - verify API connectivity and authentication.
     * 
     * @return CompletableFuture<Boolean> true if API is reachable
     */
    public CompletableFuture<Boolean> healthCheck() {
        org.json.JSONObject json = new org.json.JSONObject();
        json.put("prompt", "Hello");
        json.put("max_tokens", 10);
        
        return sendCourseRequest(json.toString())
                .thenApply(response -> {
                    System.out.println("✓ AI API Health Check Passed");
                    return true;
                })
                .exceptionally(ex -> {
                    System.err.println("✗ AI API Health Check Failed: " + ex.getMessage());
                    return false;
                });
    }
}
