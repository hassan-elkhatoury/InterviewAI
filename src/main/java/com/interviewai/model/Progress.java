package com.interviewai.model;

/**
 * Tracks user progression and XP in a course.
 * TODO: Integrate with ProgressService and ProgressDAO.
 */
public class Progress {
    private int id;
    private int userId;
    private int courseId;
    private int xp; // total XP
    private int streak; // consecutive days

    // getters/setters omitted intentionally for scaffold
}
