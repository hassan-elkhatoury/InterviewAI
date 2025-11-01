package com.interviewai.model;

/**
 * Represents a gamification achievement (badges, milestones).
 * TODO: Wire with AchievementService and persistence.
 */
public class Achievement {
    private int id;
    private String code; // e.g., "STREAK_7"
    private String name;
    private String description;
}
