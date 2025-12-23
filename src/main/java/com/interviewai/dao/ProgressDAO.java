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
     * Counts consecutive days from today/yesterday backwards.
     * Streak resets to 0 if user skips a day.
     */
    public int calculateUserStreak(int userId) throws SQLException {
        // Get all unique days where user had activity (gained > 0 XP)
        // Ordered by date DESC (newest first)
        String query = "SELECT DISTINCT DATE(last_updated) as activity_date " +
                       "FROM progress " +
                       "WHERE user_id = ? AND xp > 0 " +
                       "ORDER BY activity_date DESC";
                       
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            List<LocalDate> activityDates = new ArrayList<>();
            while (rs.next()) {
                Date sqlDate = rs.getDate("activity_date");
                if (sqlDate != null) {
                    activityDates.add(sqlDate.toLocalDate());
                }
            }
            
            if (activityDates.isEmpty()) {
                return 0;
            }
            
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            
            // Check if streak is active (activity today OR yesterday)
            // If the most recent activity was older than yesterday, streak is broken -> 0
            LocalDate mostRecent = activityDates.get(0);
            if (!mostRecent.equals(today) && !mostRecent.equals(yesterday)) {
                return 0;
            }
            
            int streak = 0;
            LocalDate expectedDate = mostRecent.equals(today) ? today : yesterday;
            
            for (LocalDate date : activityDates) {
                if (date.equals(expectedDate)) {
                    streak++;
                    expectedDate = expectedDate.minusDays(1); // Set expected next date to previous day
                } else if (date.isBefore(expectedDate)) {
                    // Gap found, stop counting
                    break;
                }
                // If date is after expectedDate, it shouldn't happen with sorted DESC logic if we start correctly,
                // but checking isBefore handles gaps.
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
        // Count questions answered since last quest reset (claim)
        String query = "SELECT COUNT(*) as count FROM questions q " +
                       "JOIN chapters c ON q.chapter_id = c.id " +
                       "JOIN generated_courses gc ON c.course_id = gc.id " +
                       "JOIN users u ON gc.user_id = u.id " +
                       "WHERE gc.user_id = ? " +
                       "AND (q.status = 'COMPLETED' OR q.status = 'INCORRECT' OR q.status = 'IN_PROGRESS') " +
                       "AND q.updated_at > COALESCE(u.daily_quest_last_reset, DATE_SUB(NOW(), INTERVAL 1 DAY))";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("DEBUG PROGRESDAO: Questions since last reset for User " + userId + " is " + count);
                return count;
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

    /**
     * Check if user has claimed the daily quest reward today
     */
    public boolean hasClaimedDailyQuest(int userId) throws SQLException {
        String query = "SELECT last_daily_quest_claim FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Date lastClaim = rs.getDate("last_daily_quest_claim");
                if (lastClaim != null) {
                    return lastClaim.toLocalDate().equals(LocalDate.now());
                }
            }
        }
        return false;
    }

    /**
     * Check if user has claimed the monthly quest reward this month
     */
    public boolean hasClaimedMonthlyQuest(int userId) throws SQLException {
        String query = "SELECT last_monthly_quest_claim FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Date lastClaim = rs.getDate("last_monthly_quest_claim");
                if (lastClaim != null) {
                    LocalDate claimDate = lastClaim.toLocalDate();
                    LocalDate now = LocalDate.now();
                    return claimDate.getMonth() == now.getMonth() && claimDate.getYear() == now.getYear();
                }
            }
        }
        return false;
    }

    /**
     * Claim daily quest reward
     */
    public void claimDailyQuest(int userId, int xpReward) throws SQLException {
        // Reset the quest progress by updating the last reset timestamp
        String query = "UPDATE users SET " + 
                       "daily_quest_last_reset = NOW(), " +
                       "last_daily_quest_claim = CURDATE() " +
                       "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
        addXpToLatestCourse(userId, xpReward);
    }

    public int getDailyQuestClaimsCount(int userId) throws SQLException {
         String query = "SELECT daily_quest_claims_count, last_daily_quest_claim FROM users WHERE id = ?";
         try (Connection conn = DBConnection.getConnection();
              PreparedStatement stmt = conn.prepareStatement(query)) {
             stmt.setInt(1, userId);
             ResultSet rs = stmt.executeQuery();
             if (rs.next()) {
                 Date lastClaim = rs.getDate("last_daily_quest_claim");
                 if (lastClaim != null && lastClaim.toLocalDate().equals(LocalDate.now())) {
                     return rs.getInt("daily_quest_claims_count");
                 }
             }
         }
         return 0;
    }

    /**
     * Reset daily quest progress by updating the reset timestamp
     * This makes the question count query return 0
     */
    public void resetDailyQuestProgress(int userId) throws SQLException {
        String query = "UPDATE users SET daily_quest_last_reset = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Claim monthly quest reward
     */
    public void claimMonthlyQuest(int userId, int xpReward) throws SQLException {
        String query = "UPDATE users SET last_monthly_quest_claim = CURDATE() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
        addXpToLatestCourse(userId, xpReward);
    }

    /**
     * Add XP to the user's latest active course
     */
    private void addXpToLatestCourse(int userId, int xp) throws SQLException {
        String findCourse = "SELECT id FROM generated_courses WHERE user_id = ? AND status = 'ACTIVE' ORDER BY created_at DESC LIMIT 1";
        int courseId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(findCourse)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                courseId = rs.getInt("id");
            }
        }
        
        if (courseId == -1) {
             // Fallback: try to find ANY course
             String findAnyCourse = "SELECT id FROM generated_courses WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
             try (Connection conn = DBConnection.getConnection();
                  PreparedStatement stmt = conn.prepareStatement(findAnyCourse)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    courseId = rs.getInt("id");
                }
            }
        }

        if (courseId != -1) {
            saveProgress(userId, courseId, xp);
        }
    }

    /**
     * Get number of chapters completed by user this month
     */
    public int getChaptersCompletedThisMonth(int userId) throws SQLException {
        // Note: This requires the 'completed_at' column in chapters table
        String query = "SELECT COUNT(*) as count FROM chapters c " +
                       "JOIN generated_courses gc ON c.course_id = gc.id " +
                       "WHERE gc.user_id = ? " +
                       "AND c.status = 'COMPLETED' " +
                       "AND MONTH(c.completed_at) = MONTH(CURDATE()) " +
                       "AND YEAR(c.completed_at) = YEAR(CURDATE())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("DEBUG PROGRESSDAO: Chapters completed this month for User " + userId + " is " + count);
                return count;
            }
        }
        return 0;
    }

    /**
     * Calculate user's overall accuracy (correct answers / total answers)
     */
    public double calculateAccuracy(int userId) throws SQLException {
        String query = "SELECT " +
                       "COUNT(CASE WHEN q.status = 'COMPLETED' THEN 1 END) as correct_count, " +
                       "COUNT(*) as total_count " +
                       "FROM questions q " +
                       "JOIN chapters c ON q.chapter_id = c.id " +
                       "JOIN generated_courses gc ON c.course_id = gc.id " +
                       "WHERE gc.user_id = ? " +
                       "AND (q.status = 'COMPLETED' OR q.status = 'INCORRECT')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int correct = rs.getInt("correct_count");
                int total = rs.getInt("total_count");
                if (total > 0) {
                    return (double) correct / total * 100.0;
                }
            }
        }
        return 0.0;
    }

    /**
     * Get total number of questions answered by user
     */
    public int getTotalQuestionsAnswered(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM questions q " +
                       "JOIN chapters c ON q.chapter_id = c.id " +
                       "JOIN generated_courses gc ON c.course_id = gc.id " +
                       "WHERE gc.user_id = ? " +
                       "AND (q.status = 'COMPLETED' OR q.status = 'INCORRECT')";
        
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
     * Get total number of questions available for user
     */
    public int getTotalQuestionsAvailable(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM questions q " +
                       "JOIN chapters c ON q.chapter_id = c.id " +
                       "JOIN generated_courses gc ON c.course_id = gc.id " +
                       "WHERE gc.user_id = ?";
        
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
     * Get estimated time spent by user in minutes
     * Based on number of questions answered (assuming 2 mins per question)
     * and lessons completed (assuming 15 mins per lesson)
     */
    public int getEstimatedTimeSpent(int userId) throws SQLException {
        int questionsAnswered = getTotalQuestionsAnswered(userId);
        int lessonsCompleted = getLessonsCompletedByUser(userId);
        
        return (questionsAnswered * 2) + (lessonsCompleted * 15);
    }

    /**
     * Get total number of courses enrolled by user
     */
    public int getTotalCoursesEnrolled(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM generated_courses WHERE user_id = ?";
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
}
