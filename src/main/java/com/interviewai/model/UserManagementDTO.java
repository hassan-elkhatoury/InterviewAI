package com.interviewai.model;

import java.sql.Timestamp;
import java.util.List;

/**
 * Extended user data model for admin user management.
 * Contains all user data including onboarding details and progress information.
 */
public class UserManagementDTO {
    // Basic user info
    private int id;
    private String username;
    private String email;
    private String role;
    private boolean isActive;
    private boolean twoFactorEnabled;
    private Timestamp createdAt;
    private Timestamp lastActive;
    
    // Onboarding data
    private String interviewType;
    private String language;
    private String timeline;
    private String context;
    private String cvPath;
    
    // Progress data
    private int totalXP;
    private int completedLessons;
    private int enrolledCourses;
    private int currentStreak;
    private int badgeCount;
    private double quizAvgScore;
    private int simulationScore;
    
    // Premium status
    private boolean isPremium;
    
    // Badges
    private List<String> badges;
    
    public UserManagementDTO() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getLastActive() { return lastActive; }
    public void setLastActive(Timestamp lastActive) { this.lastActive = lastActive; }
    
    public String getInterviewType() { return interviewType; }
    public void setInterviewType(String interviewType) { this.interviewType = interviewType; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getTimeline() { return timeline; }
    public void setTimeline(String timeline) { this.timeline = timeline; }
    
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    
    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }
    
    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }
    
    public int getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(int completedLessons) { this.completedLessons = completedLessons; }
    
    public int getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(int enrolledCourses) { this.enrolledCourses = enrolledCourses; }
    
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    
    public int getBadgeCount() { return badgeCount; }
    public void setBadgeCount(int badgeCount) { this.badgeCount = badgeCount; }
    
    public double getQuizAvgScore() { return quizAvgScore; }
    public void setQuizAvgScore(double quizAvgScore) { this.quizAvgScore = quizAvgScore; }
    
    public int getSimulationScore() { return simulationScore; }
    public void setSimulationScore(int simulationScore) { this.simulationScore = simulationScore; }
    
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
    
    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }
    
    // Helper method to get status string
    public String getStatusString() {
        return isActive ? "Active" : "Suspended";
    }
    
    // Helper method to get avatar initials
    public String getInitials() {
        if (username == null || username.isEmpty()) {
            return "?";
        }
        String[] parts = username.split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }
}
