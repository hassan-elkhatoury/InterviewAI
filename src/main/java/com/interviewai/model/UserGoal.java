package com.interviewai.model;

import java.time.LocalDateTime;

/**
 * Model class representing a user's custom goal
 */
public class UserGoal {
    private int id;
    private int userId;
    private String goalName;
    private String goalType; // QUESTIONS, XP, CHAPTERS, TIME, COURSES
    private int targetValue;
    private int currentValue;
    private int startValue;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public UserGoal() {}

    public UserGoal(int userId, String goalName, String goalType, int targetValue) {
        this.userId = userId;
        this.goalName = goalName;
        this.goalType = goalType;
        this.targetValue = targetValue;
        this.currentValue = 0;
        this.startValue = 0;
        this.isActive = true;
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

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public int getStartValue() {
        return startValue;
    }

    public void setStartValue(int startValue) {
        this.startValue = startValue;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Calculate progress percentage
     */
    public double getProgressPercentage() {
        if (targetValue == 0) return 0;
        return Math.min(100.0, (double) currentValue / targetValue * 100.0);
    }

    /**
     * Check if goal is completed
     */
    public boolean isCompleted() {
        return currentValue >= targetValue;
    }

    @Override
    public String toString() {
        return "UserGoal{" +
                "id=" + id +
                ", userId=" + userId +
                ", goalName='" + goalName + '\'' +
                ", goalType='" + goalType + '\'' +
                ", targetValue=" + targetValue +
                ", currentValue=" + currentValue +
                ", progress=" + String.format("%.1f%%", getProgressPercentage()) +
                '}';
    }
}
