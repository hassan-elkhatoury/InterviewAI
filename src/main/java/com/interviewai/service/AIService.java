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
 * Simple AI service to send requests to Gemini API and get responses.
 */
public class AIService {
    
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    
    private final String apiUrl;
    private final String apiKey;
    
    public AIService() {
        // Load from config
        this.apiUrl = ConfigLoader.get("ai.api.url");
        this.apiKey = ConfigLoader.get("ai.api.key");
        
        if (apiUrl == null || apiKey == null) {
            throw new RuntimeException("AI API not configured in config.properties");
        }
    }
    
    /**
     * Send a prompt to Gemini and get JSON response.
     * 
     * @param prompt The prompt text to send
     * @return JSON response as String
     */
    public String sendRequest(String prompt) {
        try {
            // Build Gemini request format
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);
            
            // Add API key to URL
            String fullUrl = apiUrl + "?key=" + apiKey;
            
            // Send HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                throw new RuntimeException("API Error " + response.statusCode() + 
                        ": " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build a course generation prompt based on user's onboarding data.
     */
    public String buildCoursePrompt(String interviewType, String language, 
                                   String timeline, String context) {
        return String.format(
            "Generate a comprehensive interview preparation course for %s position/program applying for a %s. " +
            "User preparation timeline: %s\n" +
            "Desired language: %s\n\n" +
            "Create a detailed course with the following structure:\n" +
            "- Create 3-5 chapters (each chapter represents a topic area)\n" +
            "- Each chapter must have a clear name and description\n" +
            "- Each chapter contains 4-6 multiple-choice questions\n" +
            "- Each question must have exactly 4 choices (A, B, C, D)\n" +
            "- Clearly mark the correct answer choice\n" +
            "- Include brief explanations for why answers are correct\n\n" +
            "Return the complete response in valid JSON format with this exact structure:\n" +
            "{\n" +
            "  \"course_title\": \"[Generated title]\",\n" +
            "  \"chapters\": [\n" +
            "    {\n" +
            "      \"chapter_number\": 1,\n" +
            "      \"name\": \"[Chapter name]\",\n" +
            "      \"description\": \"[Brief description]\",\n" +
            "      \"questions\": [\n" +
            "        {\n" +
            "          \"id\": 1,\n" +
            "          \"question\": \"[Question text]\",\n" +
            "          \"choices\": [\"Choice A\", \"Choice B\", \"Choice C\", \"Choice D\"],\n" +
            "          \"correct_answer\": \"Choice A\",\n" +
            "          \"explanation\": \"[Why this is correct]\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}",
            context, 
            mapInterviewType(interviewType), 
            mapTimeline(timeline), 
            mapLanguage(language)
        );
    }
    
    private String mapInterviewType(String type) {
        switch (type) {
            case "JOB": return "Job";
            case "VISA": return "Visa";
            case "INTERNSHIP": return "Internship";
            case "UNIVERSITY": return "University";
            default: return type;
        }
    }
    
    private String mapLanguage(String lang) {
        switch (lang) {
            case "ENGLISH": return "English";
            case "FRENCH": return "French";
            case "ARABIC": return "Arabic";
            case "SPANISH": return "Spanish";
            default: return lang;
        }
    }
    
    private String mapTimeline(String timeline) {
        switch (timeline) {
            case "TOMORROW": return "Intensive - Interview tomorrow";
            case "THIS_WEEK": return "Moderate - Interview this week";
            case "LATER": return "Relaxed - Interview later";
            default: return timeline;
        }
    }
}
