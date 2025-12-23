package com.interviewai.model;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for admin course management.
 * Contains all course information needed for the admin courses view.
 */
public class CourseManagementDTO {
    private int id;
    private int userId;
    private String username;
    private String userEmail;
    private String courseTitle;
    private String status;
    private LocalDateTime createdAt;
    private int chaptersCount;
    private int completedChapters;
    private int questionsCount;
    private double completionPercentage;
    
    // Constructor
    public CourseManagementDTO() {}
    
    public CourseManagementDTO(int id, int userId, String username, String userEmail,
                               String courseTitle, String status, LocalDateTime createdAt,
                               int chaptersCount, int completedChapters, int questionsCount) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
        this.courseTitle = courseTitle;
        this.status = status;
        this.createdAt = createdAt;
        this.chaptersCount = chaptersCount;
        this.completedChapters = completedChapters;
        this.questionsCount = questionsCount;
        this.completionPercentage = chaptersCount > 0 
            ? (double) completedChapters / chaptersCount * 100 
            : 0.0;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public int getChaptersCount() { return chaptersCount; }
    public void setChaptersCount(int chaptersCount) { 
        this.chaptersCount = chaptersCount;
        recalculateCompletion();
    }
    
    public int getCompletedChapters() { return completedChapters; }
    public void setCompletedChapters(int completedChapters) { 
        this.completedChapters = completedChapters;
        recalculateCompletion();
    }
    
    public int getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }
    
    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { 
        this.completionPercentage = completionPercentage; 
    }
    
    private void recalculateCompletion() {
        this.completionPercentage = chaptersCount > 0 
            ? (double) completedChapters / chaptersCount * 100 
            : 0.0;
    }
    
    // Computed properties for display
    public String getFormattedDate() {
        if (createdAt == null) return "N/A";
        return createdAt.toLocalDate().toString();
    }
    
    public String getFormattedCompletion() {
        return String.format("%.0f%%", completionPercentage);
    }
    
    public String getProgressDisplay() {
        return completedChapters + "/" + chaptersCount;
    }
    
    public String getStatusBadge() {
        if (status == null) return "Unknown";
        switch (status.toUpperCase()) {
            case "ACTIVE": return "🟢 Active";
            case "COMPLETE": 
            case "COMPLETED": return "✅ Completed";
            case "ARCHIVED": return "📦 Archived";
            case "PAUSED": return "⏸️ Paused";
            default: return status;
        }
    }
    
    @Override
    public String toString() {
        return "CourseManagementDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", courseTitle='" + courseTitle + '\'' +
                ", status='" + status + '\'' +
                ", completion=" + getFormattedCompletion() +
                '}';
    }
}
