package com.interviewai.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a complete AI-generated course.
 * Contains multiple chapters with questions for interview preparation.
 */
public class GeneratedCourse {
    private int id;
    private int userId;
    private String courseTitle;
    private String interviewType;
    private String language;
    private String difficulty;
    private List<Chapter> chapters;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;           // ACTIVE, COMPLETED, ARCHIVED
    private double completionPercentage;
    
    // Constructors
    public GeneratedCourse() {}
    
    public GeneratedCourse(String courseTitle, List<Chapter> chapters) {
        this.courseTitle = courseTitle;
        this.chapters = chapters;
        this.status = "ACTIVE";
        this.completionPercentage = 0.0;
    }
    
    public GeneratedCourse(int userId, String courseTitle, String interviewType, 
                          String language, List<Chapter> chapters) {
        this.userId = userId;
        this.courseTitle = courseTitle;
        this.interviewType = interviewType;
        this.language = language;
        this.chapters = chapters;
        this.status = "ACTIVE";
        this.completionPercentage = 0.0;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
        /**
         * Parse AI response JSON into a GeneratedCourse object.
         * Handles Gemini API response format with candidates/content/parts structure.
         */
        public static GeneratedCourse fromJson(String json, int userId) {
            try {
                org.json.JSONObject response = new org.json.JSONObject(json);
                
                // Extract the actual JSON course from Gemini's response
                String courseJson = json;
                if (response.has("candidates")) {
                    // Gemini API format: candidates[0].content.parts[0].text
                    org.json.JSONArray candidates = response.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        org.json.JSONObject candidate = candidates.getJSONObject(0);
                        org.json.JSONObject content = candidate.getJSONObject("content");
                        org.json.JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            courseJson = parts.getJSONObject(0).getString("text");
                            // Remove markdown code blocks if present
                            courseJson = courseJson.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
                        }
                    }
                }
                
                System.out.println("Parsing course JSON: " + courseJson.substring(0, Math.min(200, courseJson.length())) + "...");
                
                org.json.JSONObject obj = new org.json.JSONObject(courseJson);
                String courseTitle = obj.optString("course_title", "Untitled Course");
                java.util.List<Chapter> chapters = new java.util.ArrayList<>();
                org.json.JSONArray chaptersArr = obj.optJSONArray("chapters");
                
                System.out.println("Found " + (chaptersArr != null ? chaptersArr.length() : 0) + " chapters");
                
                if (chaptersArr != null) {
                    for (int i = 0; i < chaptersArr.length(); i++) {
                        org.json.JSONObject chObj = chaptersArr.getJSONObject(i);
                        int chapterNumber = chObj.optInt("chapter_number", i+1);
                        String name = chObj.optString("name", "");
                        String description = chObj.optString("description", "");
                        java.util.List<Question> questions = new java.util.ArrayList<>();
                        org.json.JSONArray questionsArr = chObj.optJSONArray("questions");
                        
                        System.out.println("Chapter " + chapterNumber + ": " + name + " - " + (questionsArr != null ? questionsArr.length() : 0) + " questions");
                        
                        if (questionsArr != null) {
                            for (int j = 0; j < questionsArr.length(); j++) {
                                org.json.JSONObject qObj = questionsArr.getJSONObject(j);
                                int qId = qObj.optInt("id", j+1);
                                String questionText = qObj.optString("question", "");
                                java.util.List<String> choices = new java.util.ArrayList<>();
                                org.json.JSONArray choicesArr = qObj.optJSONArray("choices");
                                if (choicesArr != null) {
                                    for (int k = 0; k < choicesArr.length(); k++) {
                                        choices.add(choicesArr.getString(k));
                                    }
                                }
                                String correctAnswer = qObj.optString("correct_answer", "");
                                String explanation = qObj.optString("explanation", "");
                                Question q = new Question(qId, questionText, choices, correctAnswer, explanation);
                                questions.add(q);
                            }
                        }
                        Chapter chapter = new Chapter(chapterNumber, name, description, questions);
                        chapters.add(chapter);
                    }
                }
                GeneratedCourse course = new GeneratedCourse(userId, courseTitle, null, null, chapters);
                System.out.println("Parsed course: " + courseTitle + " with " + chapters.size() + " chapters");
                return course;
            } catch (Exception e) {
                System.err.println("JSON parsing error: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to parse AI course JSON: " + e.getMessage(), e);
            }
        }
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getCourseTitle() {
        return courseTitle;
    }
    
    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }
    
    public String getInterviewType() {
        return interviewType;
    }
    
    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public List<Chapter> getChapters() {
        return chapters;
    }
    
    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public double getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
    
    public int getTotalQuestions() {
        return chapters != null ? chapters.stream()
                .mapToInt(Chapter::getQuestionCount)
                .sum() : 0;
    }
    
    public int getTotalChapters() {
        return chapters != null ? chapters.size() : 0;
    }
    
    @Override
    public String toString() {
        return "GeneratedCourse{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseTitle='" + courseTitle + '\'' +
                ", interviewType='" + interviewType + '\'' +
                ", language='" + language + '\'' +
                ", chapters=" + getTotalChapters() +
                ", questions=" + getTotalQuestions() +
                ", status='" + status + '\'' +
                ", completion=" + completionPercentage + "%" +
                '}';
    }
}
