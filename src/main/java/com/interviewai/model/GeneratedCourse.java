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
