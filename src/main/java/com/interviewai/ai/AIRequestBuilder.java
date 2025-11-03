package com.interviewai.ai;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Builds JSON request bodies for AI course generation.
 * Creates structured prompts for Google Gemini API.
 */
public class AIRequestBuilder {

    /**
     * Builds a comprehensive course generation request for Gemini API.
     * 
     * @param interviewType Type of interview (JOB, VISA, INTERNSHIP, UNIVERSITY)
     * @param language Interview language (ENGLISH, FRENCH, ARABIC, SPANISH)
     * @param timeline Interview urgency (TOMORROW, THIS_WEEK, LATER)
     * @param context User's specific role/program/position
     * @return JSON string with structured prompt in Gemini format
     */
    public static String buildCourseRequest(String interviewType, String language, 
                                            String timeline, String context) {
        // Map enum values to readable strings
        String typeLabel = mapInterviewType(interviewType);
        String levelLabel = mapTimeline(timeline);
        String languageLabel = mapLanguage(language);
        
        String prompt = String.format(
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
            "}\n\n" +
            "Ensure questions are %s appropriate and match the user's background: %s",
            context, typeLabel, levelLabel, languageLabel, 
            levelLabel.toLowerCase(), context
        );
        
        // Build Gemini API format: contents[] -> parts[] -> text
        JSONObject json = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        json.put("contents", contents);
        
        return json.toString();
    }
    
    /**
     * Builds a follow-up request for additional practice questions in Gemini format.
     */
    public static String buildPracticeQuestionsRequest(String chapter, String difficulty) {
        String difficultyLabel = mapDifficulty(difficulty);
        
        String prompt = String.format(
            "Generate 5 additional %s-level multiple-choice practice questions for the '%s' chapter.\n\n" +
            "Format each question as JSON:\n" +
            "{\n" +
            "  \"id\": [sequential],\n" +
            "  \"question\": \"[Question]\",\n" +
            "  \"choices\": [\"A\", \"B\", \"C\", \"D\"],\n" +
            "  \"correct_answer\": \"[Answer]\",\n" +
            "  \"explanation\": \"[Explanation]\"\n" +
            "}\n\n" +
            "Return as a JSON array.",
            difficultyLabel, chapter
        );
        
        // Build Gemini API format: contents[] -> parts[] -> text
        JSONObject json = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        contents.put(content);
        json.put("contents", contents);
        
        return json.toString();
    }
    
    // Helper methods for mapping enum values
    private static String mapInterviewType(String type) {
        switch (type) {
            case "JOB": return "Job";
            case "VISA": return "Visa";
            case "INTERNSHIP": return "Internship";
            case "UNIVERSITY": return "University";
            default: return type;
        }
    }
    
    private static String mapLanguage(String lang) {
        switch (lang) {
            case "ENGLISH": return "English";
            case "FRENCH": return "French";
            case "ARABIC": return "Arabic";
            case "SPANISH": return "Spanish";
            default: return lang;
        }
    }
    
    private static String mapTimeline(String timeline) {
        switch (timeline) {
            case "TOMORROW": return "Intensive - Interview tomorrow";
            case "THIS_WEEK": return "Moderate - Interview this week";
            case "LATER": return "Relaxed - Interview later";
            default: return timeline;
        }
    }
    
    private static String mapDifficulty(String difficulty) {
        switch (difficulty) {
            case "EASY": return "Easy";
            case "MEDIUM": return "Medium";
            case "HARD": return "Hard";
            default: return "Medium";
        }
    }
}
