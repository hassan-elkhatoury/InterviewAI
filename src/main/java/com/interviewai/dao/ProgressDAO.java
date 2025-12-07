package com.interviewai.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ProgressDAO - Handles all progress-related database operations.
 * Tracks user XP, streaks, course progress, quests, and achievements.
 */
public class ProgressDAO {
    
    /**
     * Get total XP for a user from all their progress records
     */

    
    public int getTotalXPForUser(int userId) throws SQLException {
        String query = "SELECT COALESCE(SUM(xp), 0) as total_xp FROM progress WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_xp");
            }
        }
        return 0;
    }

   public Map<String, Integer> getLast7DaysXp(int userId) throws SQLException {

    // Calculate the date 7 days ago
    LocalDate today = LocalDate.now();
    LocalDate sevenDaysAgo = today.minusDays(6); // includes today, so 7 days total

    // Initialize map with all last 7 days set to 0 XP
    Map<String, Integer> xpByDay = new LinkedHashMap<>();
    for (int i = 0; i < 7; i++) {
        LocalDate date = sevenDaysAgo.plusDays(i);
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        xpByDay.put(dayName, 0); // Initialize with 0 XP
    }

    String query =
        "SELECT xp, DATE(last_updated) AS day_date " +
        "FROM progress " +
        "WHERE user_id = ? " +
        "AND last_updated >= ? " +
        "ORDER BY last_updated ASC";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);
        stmt.setDate(2, Date.valueOf(sevenDaysAgo));
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int xp = rs.getInt("xp");
            LocalDate date = rs.getDate("day_date").toLocalDate();

            // Convert date → day abbreviation (e.g., "Mon", "Tue")
            String dayName = date.getDayOfWeek()
                                 .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            // Sum XP for that day
            xpByDay.put(dayName, xpByDay.getOrDefault(dayName, 0) + xp);
        }
    }

    return xpByDay;
}


public Map<String, Boolean> getLast7DaysProgress(int userId) throws SQLException {
    // Calculate the date 7 days ago
    LocalDate today = LocalDate.now();
    LocalDate sevenDaysAgo = today.minusDays(6); // includes today

    String query = "SELECT xp, DATE(last_updated) AS day_date " +
                   "FROM progress " +
                   "WHERE user_id = ? " +
                   "AND last_updated >= ? " +
                   "ORDER BY last_updated ASC";

    // Initialize map with all last 7 days set to false
    Map<String, Boolean> progressByDay = new LinkedHashMap<>();
    for (int i = 0; i < 7; i++) {
        LocalDate date = sevenDaysAgo.plusDays(i);
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        progressByDay.put(dayName, false);
    }

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);
        stmt.setDate(2, Date.valueOf(sevenDaysAgo));

        ResultSet rs = stmt.executeQuery();

        // Track XP per day
        Map<LocalDate, Integer> xpPerDay = new HashMap<>();
        while (rs.next()) {
            int xp = rs.getInt("xp");
            LocalDate date = rs.getDate("day_date").toLocalDate();

            xpPerDay.put(date, Math.max(xpPerDay.getOrDefault(date, 0), xp));
        }

        // Compare XP to see if any progress was made
        for (int i = 0; i < 7; i++) {
            LocalDate date = sevenDaysAgo.plusDays(i);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            // If XP exists and > 0, mark progress as true
            progressByDay.put(dayName, xpPerDay.getOrDefault(date, 0) > 0);
        }
    }

    return progressByDay;
}

    
    /**
     * Calculate user's current streak (days of consecutive activity)
     * Simplified: counts days with at least one progress entry
     */
    public int calculateUserStreak(int userId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT DATE(last_updated)) as streak_days FROM progress " +
                       "WHERE user_id = ? AND last_updated >= DATE_SUB(NOW(), INTERVAL 365 DAY) " +
                       "ORDER BY DATE(last_updated) DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("streak_days");
            }
        }
        return 0;
    }
    
    /**
     * Get all courses a user is enrolled in with progress info
     */
    public List<Map<String, Object>> getUserCourses(int userId) throws SQLException {
        List<Map<String, Object>> courses = new ArrayList<>();
        String query = "SELECT gc.id as course_id, gc.course_title, gc.created_at, " +
                       "COALESCE(MAX(p.xp), 0) as user_xp, " +
                       "ROUND((COALESCE(MAX(p.xp), 0) / 1000 * 100), 0) as progress_percentage " +
                       "FROM generated_courses gc " +
                       "LEFT JOIN progress p ON p.course_id = gc.id AND p.user_id = ? " +
                       "WHERE gc.user_id = ? AND gc.status = 'ACTIVE' " +
                       "GROUP BY gc.id, gc.course_title, gc.created_at " +
                       "ORDER BY gc.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> course = new HashMap<>();
                course.put("course_id", rs.getInt("course_id"));
                course.put("course_title", rs.getString("course_title"));
                course.put("user_xp", rs.getInt("user_xp"));
                course.put("progress_percentage", rs.getInt("progress_percentage"));
                courses.add(course);
            }
        }
        return courses;
    }
    
    /**
     * Get daily quests for user (mock implementation - in real app, fetch from quests table)
     */
    public List<Map<String, Object>> getDailyQuests(int userId) throws SQLException {
        List<Map<String, Object>> quests = new ArrayList<>();
        
        // Mock daily quests - replace with real database query if quests table exists
        Map<String, Object> quest1 = new HashMap<>();
        quest1.put("quest_name", "Answer 5 Interview Questions");
        quest1.put("required_count", 5);
        quest1.put("current_count", Math.min(5, Math.random() > 0.5 ? 3 : 2));
        quest1.put("xp_reward", 50);
        quests.add(quest1);
        
        Map<String, Object> quest2 = new HashMap<>();
        quest2.put("quest_name", "Complete 1 Full Course Chapter");
        quest2.put("required_count", 1);
        quest2.put("current_count", 0);
        quest2.put("xp_reward", 100);
        quests.add(quest2);
        
        Map<String, Object> quest3 = new HashMap<>();
        quest3.put("quest_name", "Achieve 90% Accuracy");
        quest3.put("required_count", 1);
        quest3.put("current_count", 0);
        quest3.put("xp_reward", 75);
        quests.add(quest3);
        
        return quests;
    }
    
    /**
     * Get number of lessons completed by user
     */
    public int getLessonsCompletedByUser(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as lessons_completed FROM progress WHERE user_id = ? AND xp > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("lessons_completed");
            }
        }
        return 0;
    }
    
    /**
     * Save or update user progress for a course
     */
    public void saveProgress(int userId, int courseId, int xp) throws SQLException {
        String query = "INSERT INTO progress (user_id, course_id, xp, last_updated) " +
                       "VALUES (?, ?, ?, NOW()) " +
                       "ON DUPLICATE KEY UPDATE xp = xp + ?, last_updated = NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, courseId);
            stmt.setInt(3, xp);
            stmt.setInt(4, xp);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get top learners by XP (for leaderboard)
     */
    public List<Map<String, Object>> getTopLearners(int limit) throws SQLException {
        List<Map<String, Object>> topLearners = new ArrayList<>();
        String query = "SELECT u.username, COALESCE(SUM(p.xp), 0) as total_xp " +
                       "FROM users u " +
                       "LEFT JOIN progress p ON p.user_id = u.id " +
                       "GROUP BY u.id, u.username " +
                       "ORDER BY total_xp DESC " +
                       "LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> learner = new HashMap<>();
                learner.put("username", rs.getString("username"));
                learner.put("total_xp", rs.getInt("total_xp"));
                topLearners.add(learner);
            }
        }
        return topLearners;
    }
    
    /**
     * Get total number of questions answered by user (completed questions)
     * A question is considered "answered" if its status is COMPLETED
     */
    public int getTotalQuestionsAnswered(int userId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT q.id) as total_answered " +
                       "FROM questions q " +
                       "JOIN chapters c ON c.id = q.chapter_id " +
                       "JOIN generated_courses gc ON gc.id = c.course_id " +
                       "WHERE gc.user_id = ? AND q.status = 'COMPLETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_answered");
            }
        }
        return 0;
    }
    
    /**
     * Get total number of questions available to user across all their courses
     */
    public int getTotalQuestionsAvailable(int userId) throws SQLException {
        String query = "SELECT COUNT(DISTINCT q.id) as total_questions " +
                       "FROM questions q " +
                       "JOIN chapters c ON c.id = q.chapter_id " +
                       "JOIN generated_courses gc ON gc.id = c.course_id " +
                       "WHERE gc.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_questions");
            }
        }
        return 0;
    }
    
    /**
     * Get total number of courses user is enrolled in
     */
    public int getTotalCoursesEnrolled(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as total_courses " +
                       "FROM generated_courses " +
                       "WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_courses");
            }
        }
        return 0;
    }
    
    /**
     * Calculate estimated time spent by user (10 XP = 1 minute approximation)
     * Returns time in minutes
     */
    public int getEstimatedTimeSpent(int userId) throws SQLException {
        int totalXP = getTotalXPForUser(userId);
        // Estimate: 10 XP = 1 minute
        return totalXP / 10;
    }
    
    /**
     * Calculate user accuracy rate (percentage)
     * Accuracy based on XP efficiency: Total XP / (Questions * 10)
     * Since each correct answer gives 10 XP, perfect accuracy = 100%
     * Note: This is a simplified calculation; in production you'd track correct/incorrect answers
     */
    public double calculateAccuracy(int userId) throws SQLException {
        int questionsAnswered = getTotalQuestionsAnswered(userId);
        if (questionsAnswered == 0) {
            return 0.0;
        }
        
        int totalXP = getTotalXPForUser(userId);
        int maxPossibleXP = questionsAnswered * 10; // 10 XP per correct answer
        
        if (maxPossibleXP == 0) {
            return 0.0;
        }
        
        double accuracy = (double) totalXP / maxPossibleXP * 100;
        return Math.min(accuracy, 100.0); // Cap at 100%
    }
}
