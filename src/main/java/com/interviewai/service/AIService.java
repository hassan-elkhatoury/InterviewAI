package com.interviewai.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.interviewai.model.ChapterOutline;
import com.interviewai.util.ConfigLoader;

/**
 * Simple AI service to send requests to Gemini API and get responses.
 * Supports multiple API keys with automatic rotation on rate limits.
 */
public class AIService {
    
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    
    private final String apiUrl;
    private final List<String> apiKeys;
    private int currentKeyIndex = 0;
    
    public AIService() {
        // Load from config
        this.apiUrl = ConfigLoader.get("ai.api.url");
        String keysConfig = ConfigLoader.get("ai.api.keys");
        
        if (apiUrl == null || keysConfig == null) {
            throw new RuntimeException("AI API not configured in config.properties");
        }
        
        // Parse multiple API keys (comma-separated)
        this.apiKeys = new ArrayList<>();
        for (String key : keysConfig.split(",")) {
            String trimmedKey = key.trim();
            if (!trimmedKey.isEmpty()) {
                apiKeys.add(trimmedKey);
            }
        }
        
        if (apiKeys.isEmpty()) {
            throw new RuntimeException("No valid API keys found in config.properties");
        }
        
        System.out.println("✓ Loaded " + apiKeys.size() + " API key(s) for rotation");
    }
    
    /**
     * Send a prompt to Gemini and get JSON response.
     * Automatically rotates API keys on rate limit errors (429).
     * 
     * @param prompt The prompt text to send
     * @return JSON response as String
     * @throws RuntimeException if all API keys fail
     */
    public String sendRequest(String prompt) {
        int attempts = 0;
        int maxAttempts = apiKeys.size();
        RuntimeException lastException = null;
        
        while (attempts < maxAttempts) {
            try {
                String apiKey = getCurrentApiKey();
                String response = sendRequestWithKey(prompt, apiKey);
                return response;
                
            } catch (RuntimeException e) {
                lastException = e;
                
                // Check if it's a rate limit error (429)
                if (e.getMessage().contains("429") || e.getMessage().contains("RESOURCE_EXHAUSTED")) {
                    System.err.println("⚠️  API key " + (currentKeyIndex + 1) + " rate limited. Rotating to next key...");
                    rotateToNextKey();
                    attempts++;
                    
                    // Add a small delay before retry
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // Non-rate-limit error, throw immediately
                    throw e;
                }
            }
        }
        
        // All keys exhausted
        throw new RuntimeException("All " + maxAttempts + " API key(s) exhausted. " + lastException.getMessage(), lastException);
    }
    
    /**
     * Send request using a specific API key.
     */
    private String sendRequestWithKey(String prompt, String apiKey) {
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
     * Get current API key.
     */
    private synchronized String getCurrentApiKey() {
        return apiKeys.get(currentKeyIndex);
    }
    
    /**
     * Rotate to next API key.
     */
    private synchronized void rotateToNextKey() {
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
        System.out.println("🔄 Switched to API key " + (currentKeyIndex + 1) + "/" + apiKeys.size());
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
    
    /**
     * STAGE 1: Build a prompt to generate 12 chapter outlines (without questions).
     */
    public String buildChapterOutlinesPrompt(
        String interviewType, 
        String language, 
        String timeline, 
        String context, 
        String cvContent, 
        String courseContent, 
        String courseGoal
    ) {
        return String.format(
            "You are an expert AI course designer.\n\n" +
            "Your task: Generate a comprehensive interview preparation course outline for a **%s** position or program.\n" +
            "Focus area: %s\n" +
            "User preparation timeline: %s\n" +
            "Preferred language: %s\n\n" +
            "User context: %s\n" +
            "Course goal: %s\n\n" +
            "Below is the candidate’s CV text (use it to tailor topics and skill focus):\n" +
            "\"\"\"%s\"\"\"\n\n" +
            "Instructions:\n" +
            "- Create exactly 12 chapters (each representing a topic area).\n" +
            "- Each chapter must have:\n" +
            "  * A clear and descriptive name\n" +
            "  * A detailed description of what will be covered\n\n" +
            "Return your response **as pure JSON**, with no explanations or extra text, in this exact format:\n" +
            "{\n" +
            "  \"course_title\": \"[Generated title]\",\n" +
            "  \"chapters\": [\n" +
            "    {\n" +
            "      \"chapter_number\": 1,\n" +
            "      \"name\": \"[Chapter name]\",\n" +
            "      \"description\": \"[Detailed description]\"\n" +
            "    }\n" +
            "  ]\n" +
            "}",
            mapInterviewType(interviewType),
            courseContent,
            mapTimeline(timeline),
            mapLanguage(language),
            context,
            courseGoal,
            cvContent
        );
    }
    /**
     * STAGE 2: Build a prompt to generate 2 specific chapters with questions.
     * Each chapter will have 20 multiple choice + 20 short answer questions = 40 total.
     */
    public String buildChapterQuestionsPrompt(String courseTitle, List<ChapterOutline> outlines, 
                                             int startChapterNum, int endChapterNum,
                                             String language) {
        StringBuilder chapterInfo = new StringBuilder();
        for (ChapterOutline outline : outlines) {
            if (outline.getChapterNumber() >= startChapterNum && 
                outline.getChapterNumber() <= endChapterNum) {
                chapterInfo.append(String.format(
                    "Chapter %d: %s - %s\n",
                    outline.getChapterNumber(),
                    outline.getName(),
                    outline.getDescription()
                ));
            }
        }
        
        return String.format(
            "Generate detailed questions for the following chapters of the course '%s':\n\n" +
            "%s\n" +
            "Language: %s\n\n" +
            "For EACH chapter, generate:\n" +
            "1. 20 multiple-choice questions with:\n" +
            "   - 4 short, concise answer choices (A, B, C, D)\n" +
            "   - Each choice should be a SHORT PHRASE (maximum 10-15 words)\n" +
            "   - Clear correct answer\n" +
            "   - Brief explanation\n\n" +
            "2. 20 short-answer questions with:\n" +
            "   - Questions that require SHORT ANSWERS (1-3 words or a brief phrase)\n" +
            "   - Clear correct answer (must be short and concise)\n" +
            "   - Brief explanation\n\n" +
            "Return the response in valid JSON format with this exact structure:\n" +
            "{\n" +
            "  \"chapters\": [\n" +
            "    {\n" +
            "      \"chapter_number\": %d,\n" +
            "      \"name\": \"[Chapter name]\",\n" +
            "      \"description\": \"[Brief description]\",\n" +
            "      \"questions\": [\n" +
            "        {\n" +
            "          \"id\": 1,\n" +
            "          \"question\": \"[Question text]\",\n" +
            "          \"question_type\": \"MULTIPLE_CHOICE\",\n" +
            "          \"choices\": [\"Short Choice A\", \"Short Choice B\", \"Short Choice C\", \"Short Choice D\"],\n" +
            "          \"correct_answer\": \"Short Choice A\",\n" +
            "          \"explanation\": \"[Why this is correct]\"\n" +
            "        },\n" +
            "        {\n" +
            "          \"id\": 21,\n" +
            "          \"question\": \"[Short answer question]\",\n" +
            "          \"question_type\": \"SHORT_ANSWER\",\n" +
            "          \"choices\": null,\n" +
            "          \"correct_answer\": \"[Brief answer]\",\n" +
            "          \"explanation\": \"[Why this is correct]\"\n" +
            "        },\n" +
            "        ... (20 MC + 20 SA per chapter = 40 total)\n" +
            "      ]\n" +
            "    },\n" +
            "    ... (repeat for next chapter if applicable)\n" +
            "  ]\n" +
            "}\n\n" +
            "IMPORTANT: Make sure chapters complement each other and build upon previous knowledge.",
            courseTitle,
            chapterInfo.toString(),
            mapLanguage(language),
            startChapterNum
        );
    }
    
    /**
     * Parse chapter outlines from AI response.
     */
    public List<ChapterOutline> parseChapterOutlines(String jsonResponse) {
        List<ChapterOutline> outlines = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonResponse);
            
            // Try to extract the text from Gemini's response structure
            if (root.has("candidates")) {
                JSONArray candidates = root.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    if (firstCandidate.has("content")) {
                        JSONObject content = firstCandidate.getJSONObject("content");
                        if (content.has("parts")) {
                            JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                String text = parts.getJSONObject(0).getString("text");
                                
                                // Clean up markdown code blocks if present
                                text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
                                
                                // Parse the actual JSON content
                                JSONObject courseData = new JSONObject(text);
                                JSONArray chapters = courseData.getJSONArray("chapters");
                                
                                for (int i = 0; i < chapters.length(); i++) {
                                    JSONObject chapterJson = chapters.getJSONObject(i);
                                    ChapterOutline outline = new ChapterOutline();
                                    outline.setChapterNumber(chapterJson.getInt("chapter_number"));
                                    outline.setName(chapterJson.getString("name"));
                                    outline.setDescription(chapterJson.getString("description"));
                                    outlines.add(outline);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing chapter outlines: " + e.getMessage());
            e.printStackTrace();
        }
        return outlines;
    }
}
