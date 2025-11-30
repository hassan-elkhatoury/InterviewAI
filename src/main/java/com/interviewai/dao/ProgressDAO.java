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

    String query =
        "SELECT xp, DATE(last_updated) AS day_date " +
        "FROM progress " +
        "WHERE user_id = ? " +
        "AND last_updated >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
        "ORDER BY last_updated ASC";

    Map<String, Integer> xpByDay = new LinkedHashMap<>();

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int xp = rs.getInt("xp");
            LocalDate date = rs.getDate("day_date").toLocalDate();

            // Convert date → day name (e.g., "Monday")
            String dayName = date.getDayOfWeek()
                                 .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

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
     * Counts consecutive days from today/yesterday backwards.
     * Streak resets to 0 if user skips a day.
     */
    public int calculateUserStreak(int userId) throws SQLException {
        // Get all distinct activity dates in descending order (most recent first)
        String query = "SELECT DISTINCT DATE(last_updated) as activity_date " +
                       "FROM progress " +
                       "WHERE user_id = ? " +
                       "ORDER BY activity_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            // If no activity, streak is 0
            if (!rs.next()) {
                return 0;
            }
            
            // Get today's date (date only, no time)
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            java.sql.Date yesterday = new java.sql.Date(today.getTime() - 24 * 60 * 60 * 1000);
            
            // Get the most recent activity date
            java.sql.Date mostRecentDate = rs.getDate("activity_date");
            
            // Check if the streak is still active (today or yesterday)
            // If not, streak is 0
            if (!mostRecentDate.equals(today) && !mostRecentDate.equals(yesterday)) {
                return 0;
            }
            
            // Start counting consecutive days
            int streak = 1;
            java.sql.Date expectedDate = new java.sql.Date(mostRecentDate.getTime() - 24 * 60 * 60 * 1000);
            
            // Iterate through remaining dates
            while (rs.next()) {
                java.sql.Date currentDate = rs.getDate("activity_date");
                
                // Check if current date is consecutive (one day before expected)
                if (currentDate.equals(expectedDate)) {
                    streak++;
                    // Update expected date to one day before current
                    expectedDate = new java.sql.Date(currentDate.getTime() - 24 * 60 * 60 * 1000);
                } else {
                    // Gap found, stop counting
                    break;
                }
            }
            
            return streak;
        }
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
     * Get number of questions answered by user today
     */
    public int getQuestionsAnsweredToday(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM questions q " +
                       "JOIN chapters c ON q.chapter_id = c.id " +
                       "JOIN generated_courses gc ON c.course_id = gc.id " +
                       "WHERE gc.user_id = ? " +
                       "AND (q.status = 'COMPLETED' OR q.status = 'INCORRECT') " +
                       "AND DATE(q.updated_at) = CURDATE()";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    /**
     * Get number of questions answered correctly by user today
     */
    public int getQuestionsAnsweredCorrectlyToday(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM questions q " +
                       "JOIN chapters c ON q.chapter_id = c.id " +
                       "JOIN generated_courses gc ON c.course_id = gc.id " +
                       "WHERE gc.user_id = ? " +
                       "AND q.status = 'COMPLETED' " +
                       "AND DATE(q.updated_at) = CURDATE()";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    /**
     * Get number of questions answered correctly by user this month
     */
    public int getQuestionsAnsweredCorrectlyThisMonth(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM questions q " +
                       "JOIN chapters c ON q.chapter_id = c.id " +
                       "JOIN generated_courses gc ON c.course_id = gc.id " +
                       "WHERE gc.user_id = ? " +
                       "AND q.status = 'COMPLETED' " +
                       "AND MONTH(q.updated_at) = MONTH(CURDATE()) " +
                       "AND YEAR(q.updated_at) = YEAR(CURDATE())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    /**
     * Get daily quests for user (Dynamic implementation based on real activity)
     */
    public List<Map<String, Object>> getDailyQuests(int userId) throws SQLException {
        List<Map<String, Object>> quests = new ArrayList<>();
        
        // Quest 1: Earn 50 XP (Based on correct answers today)
        // Assuming 10 XP per correct answer
        int correctToday = getQuestionsAnsweredCorrectlyToday(userId);
        int xpToday = correctToday * 10;
        
        Map<String, Object> quest1 = new HashMap<>();
        quest1.put("quest_name", "Earn 50 XP");
        quest1.put("required_count", 50);
        quest1.put("current_count", xpToday);
        quest1.put("xp_reward", 50);
        quests.add(quest1);
        
        // Quest 2: Answer 5 Questions (Any status)
        int answeredToday = getQuestionsAnsweredToday(userId);
        
        Map<String, Object> quest2 = new HashMap<>();
        quest2.put("quest_name", "Answer 5 Interview Questions");
        quest2.put("required_count", 5);
        quest2.put("current_count", answeredToday);
        quest2.put("xp_reward", 30);
        quests.add(quest2);
        
        // Quest 3: Extend Streak (Complete at least 1 question correctly today)
        // If streak > 0 and we have activity today, it's extended.
        // But for the quest progress, let's just say "Answer 1 Question Correctly"
        
        Map<String, Object> quest3 = new HashMap<>();
        quest3.put("quest_name", "Extend your streak");
        quest3.put("required_count", 1);
        quest3.put("current_count", correctToday > 0 ? 1 : 0);
        quest3.put("xp_reward", 20);
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
}
