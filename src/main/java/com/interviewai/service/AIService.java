package com.interviewai.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.interviewai.model.ChapterOutline;
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
     * Send a prompt to Gemini and get JSON response (no progress callback).
     */
    public String sendRequest(String prompt) {
        return sendRequestInternal(prompt, null, null, -1);
    }

    /**
     * Send a prompt with retry + optional progress reporting (used by multi-stage generation).
     * Retries 429 RESOURCE_EXHAUSTED errors with exponential backoff.
     * @param prompt Prompt text
     * @param progressCallback Optional progress callback (can be null)
     * @param phaseLabel Short label to show when retrying
     * @param percentBase Percent value to reuse when emitting retry messages
     */
    public String sendRequestWithProgress(String prompt,
                                          MultiStageAIService.ProgressCallback progressCallback,
                                          String phaseLabel,
                                          int percentBase) {
        return sendRequestInternal(prompt, progressCallback, phaseLabel, percentBase);
    }

    private String sendRequestInternal(String prompt,
                                       MultiStageAIService.ProgressCallback progressCallback,
                                       String phaseLabel,
                                       int percentBase) {
        final int maxRetries = 3;
        int attempt = 0;
        while (true) {
            attempt++;
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

                String fullUrl = apiUrl + "?key=" + apiKey;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(fullUrl))
                        .timeout(Duration.ofSeconds(60))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return response.body();
                }

                // Rate limit / resource exhausted handling (429)
                if (status == 429 || response.body().contains("RESOURCE_EXHAUSTED")) {
                    if (attempt <= maxRetries) {
                        long backoffMs = (long) (1000L * Math.pow(2, attempt - 1)); // 1s,2s,4s
                        String msg = String.format("Rate limit hit (%s). Retrying in %d ms (attempt %d/%d)...",
                                status == 429 ? "429" : "RESOURCE_EXHAUSTED", backoffMs, attempt, maxRetries);
                        System.out.println(msg);
                        if (progressCallback != null && phaseLabel != null) {
                            int percent = percentBase >= 0 ? percentBase : 0;
                            progressCallback.onProgress(phaseLabel + " - " + msg, percent);
                        }
                        TimeUnit.MILLISECONDS.sleep(backoffMs);
                        continue; // retry
                    } else {
                        throw new RuntimeException("API Error 429 after retries: " + response.body());
                    }
                }

                throw new RuntimeException("API Error " + status + ": " + response.body());
            } catch (Exception e) {
                // Non-rate-limit or exhausted after retries
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    throw new RuntimeException("Failed to call AI API: " + e.getMessage(), e);
                }
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Failed to call AI API: " + e.getMessage(), e);
                }
                // Transient network error, brief backoff then retry
                long backoffMs = (long) (800L * Math.pow(2, attempt - 1));
                System.out.println("Transient error: " + e.getMessage() + ". Retrying in " + backoffMs + " ms (attempt " + attempt + "/" + maxRetries + ")...");
                try { TimeUnit.MILLISECONDS.sleep(backoffMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
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
    public String buildChapterOutlinesPrompt(String interviewType, String language, 
                                             String timeline, String context) {
        return String.format(
            "Generate a comprehensive interview preparation course outline for %s position/program applying for a %s. " +
            "User preparation timeline: %s\n" +
            "Desired language: %s\n\n" +
            "Create exactly 12 chapters (each chapter represents a topic area).\n" +
            "Each chapter must have:\n" +
            "- A clear, descriptive name\n" +
            "- A detailed description of what will be covered\n\n" +
            "Return the response in valid JSON format with this exact structure:\n" +
            "{\n" +
            "  \"course_title\": \"[Generated title]\",\n" +
            "  \"chapters\": [\n" +
            "    {\n" +
            "      \"chapter_number\": 1,\n" +
            "      \"name\": \"[Chapter name]\",\n" +
            "      \"description\": \"[Detailed description of chapter content]\"\n" +
            "    },\n" +
            "    ... (repeat for all 12 chapters)\n" +
            "  ]\n" +
            "}",
            context, 
            mapInterviewType(interviewType), 
            mapTimeline(timeline), 
            mapLanguage(language)
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
