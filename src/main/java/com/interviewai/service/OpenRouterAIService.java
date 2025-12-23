package com.interviewai.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.JSONArray;
import org.json.JSONObject;

import com.interviewai.util.ConfigLoader;

/**
 * Service to interact with OpenRouter API for AI-powered answer validation.
 * Uses Llama 3.3 70B Instruct model for fast, accurate scoring.
 */
public class OpenRouterAIService {
    
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1) // Use HTTP/1.1 for better compatibility
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private final String apiUrl;
    private final String apiKey;
    private final String model;
    
    public OpenRouterAIService() {
        // Load from config
        this.apiUrl = ConfigLoader.get("openrouter.api.url");
        this.apiKey = ConfigLoader.get("openrouter.api.key");
        this.model = ConfigLoader.get("openrouter.model");
        
        if (apiUrl == null || apiKey == null || model == null) {
            throw new RuntimeException("OpenRouter API not configured in config.properties");
        }
        
        System.out.println("✓ OpenRouter AI Service initialized with model: " + model);
    }
    
    /**
     * Validate a user's answer against the correct answer.
     * Returns a score (0-100) and detailed feedback.
     */
    public ValidationResult validateAnswer(String question, String correctAnswer, String userAnswer) {
        try {
            // Build the prompt for AI - Direct and personal with strict scoring
            String systemPrompt = "You are an interview coach. Speak DIRECTLY to the candidate using 'you' and 'your'. " +
                "Never use 'the candidate' or 'they'. Respond ONLY with valid JSON (no markdown). " +
                "Format: {\"score\": 85, \"strengths\": \"You did...\", \"improvements\": \"You should...\"}. " +
                "SCORING RULES: " +
                "- Gibberish/random words/irrelevant = 0-10 " +
                "- Wrong concept = 10-30 " +
                "- Partially correct = 40-60 " +
                "- Mostly correct = 70-85 " +
                "- Perfect = 90-100. " +
                "Be STRICT. Use 'you/your' in ALL feedback!";
            
            String userPrompt = String.format(
                "Question: %s\nExpected: %s\nAnswer: %s\n\n" +
                "Evaluate and respond in JSON. Score 0-100.\n" +
                "Strengths: What YOU did well (use 'you/your')\n" +
                "Improvements: What YOU should improve (use 'you/your')\n" +
                "Remember: Use 'you' and 'your', NOT 'the candidate'!",
                question, correctAnswer, userAnswer
            );
            
            // Build request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));
            messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userPrompt));
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 300); // Reduced for faster response
            
            System.out.println("Sending request to: " + apiUrl);
            System.out.println("Request body: " + requestBody.toString());
            
            // Send request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "http://localhost:8080") // Required by OpenRouter
                    .header("X-Title", "InterviewAI") // Optional but recommended
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            
            System.out.println("Sending HTTP request...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response status: " + response.statusCode());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseResponse(response.body());
            } else {
                System.err.println("API Error Response: " + response.body());
                throw new RuntimeException("API Error " + response.statusCode() + ": " + response.body());
            }
            
        } catch (Exception e) {
            System.err.println("Error calling OpenRouter API: " + e.getMessage());
            e.printStackTrace();
            
            // Return fallback result with more helpful message
            return new ValidationResult(
                50,
                "AI validation temporarily unavailable",
                "Error: " + e.getClass().getSimpleName() + " - Please try again"
            );
        }
    }
    
    /**
     * Get a detailed technical explanation of the question and correct answer.
     * Returns plain text explanation personalized based on user's answer.
     */
    public String explainQuestion(String question, String correctAnswer, String userAnswer) {
        try {
            // Build prompt for technical explanation - ultra-concise and personalized
            String systemPrompt = "You are a technical expert. Give SHORT, direct explanations. " +
                "Start by telling if their answer was correct or incorrect. " +
                "No fluff. Just key points and examples. Plain text only.";
            
            String userPrompt = String.format(
                "Question: %s\n" +
                "Correct Answer: %s\n" +
                "User's Answer: %s\n\n" +
                "First, tell them if their answer was CORRECT or INCORRECT.\n" +
                "Then explain in 3-4 SHORT sentences:\n" +
                "1. What the concept means\n" +
                "2. Quick example\n" +
                "3. Why it matters\n\n" +
                "Max 100 words. Be brief and direct!",
                question, correctAnswer, userAnswer
            );
            
            // Build request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                .put("role", "system")
                .put("content", systemPrompt));
            messages.put(new JSONObject()
                .put("role", "user")
                .put("content", userPrompt));
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.5);
            requestBody.put("max_tokens", 300); // Increased for better explanations
            
            // Send request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "InterviewAI")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JSONObject root = new JSONObject(response.body());
                
                // Extract content
                if (root.has("choices")) {
                    JSONArray choices = root.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject message = firstChoice.getJSONObject("message");
                        return message.getString("content").trim();
                    }
                }
                
                return "Unable to generate explanation (No content). Response: " + response.body();
            } else {
                System.err.println("API Error: " + response.statusCode() + " " + response.body());
                return "AI Error: " + response.statusCode() + " - " + response.body();
            }
            
        } catch (Exception e) {
            System.err.println("Error getting explanation: " + e.getMessage());
            e.printStackTrace();
            return "Unable to generate explanation. Error: " + e.getMessage();
        }
    }
    
    /**
     * Parse the AI response and extract score and feedback.
     */
    private ValidationResult parseResponse(String responseBody) {
        try {
            // Debug: Print the full response
            System.out.println("=== AI API Response ===");
            System.out.println(responseBody);
            System.out.println("======================");
            
            JSONObject root = new JSONObject(responseBody);
            
            // Try different response formats
            String content = null;
            
            // Format 1: Standard OpenAI format with "choices"
            if (root.has("choices")) {
                JSONArray choices = root.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    content = message.getString("content");
                }
            }
            // Format 2: Direct "content" field
            else if (root.has("content")) {
                content = root.getString("content");
            }
            // Format 3: "text" field
            else if (root.has("text")) {
                content = root.getString("text");
            }
            // Format 4: "response" field
            else if (root.has("response")) {
                content = root.getString("response");
            }
            
            if (content != null && !content.isEmpty()) {
                // Try to parse as JSON first
                try {
                    // Remove markdown code blocks if present
                    content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
                    
                    JSONObject aiResponse = new JSONObject(content);
                    int score = aiResponse.getInt("score");
                    String strengths = aiResponse.optString("strengths", "Good effort!");
                    String improvements = aiResponse.optString("improvements", "Keep practicing!");
                    
                    return new ValidationResult(score, strengths, improvements);
                    
                } catch (Exception e) {
                    // If JSON parsing fails, try to extract info from text
                    System.out.println("JSON parsing failed, trying text extraction...");
                    return parseTextResponse(content);
                }
            }
            
            // Fallback
            System.err.println("No content found in response. Response keys: " + root.keySet());
            return new ValidationResult(50, "Response received but no content found", "Unable to parse feedback");
            
        } catch (Exception e) {
            System.err.println("Error parsing AI response: " + e.getMessage());
            e.printStackTrace();
            return new ValidationResult(50, "Error parsing response", e.getMessage());
        }
    }
    
    /**
     * Parse text response if JSON parsing fails.
     */
    private ValidationResult parseTextResponse(String content) {
        int score = 50; // Default
        String strengths = "";
        String improvements = "";
        
        // Try to extract score
        if (content.contains("score") || content.contains("Score")) {
            String[] lines = content.split("\\n");
            for (String line : lines) {
                if (line.toLowerCase().contains("score")) {
                    // Extract number
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        try {
                            score = Integer.parseInt(parts[1].trim().replaceAll("[^0-9]", ""));
                        } catch (Exception e) {
                            // Keep default
                        }
                    }
                }
            }
        }
        
        // Use the full content as feedback
        if (content.length() > 200) {
            strengths = content.substring(0, 100) + "...";
            improvements = "See full feedback above";
        } else {
            strengths = content;
            improvements = "Keep practicing!";
        }
        
        return new ValidationResult(score, strengths, improvements);
    }
    
    /**
     * Result class containing validation score and feedback.
     */
    public static class ValidationResult {
        private final int score;
        private final String strengths;
        private final String improvements;
        
        public ValidationResult(int score, String strengths, String improvements) {
            this.score = Math.max(0, Math.min(100, score)); // Clamp to 0-100
            this.strengths = strengths;
            this.improvements = improvements;
        }
        
        public int getScore() { return score; }
        public String getStrengths() { return strengths; }
        public String getImprovements() { return improvements; }
        
        @Override
        public String toString() {
            return String.format("Score: %d/100\nStrengths: %s\nImprovements: %s", 
                score, strengths, improvements);
        }
    }
}
