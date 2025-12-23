package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for fetching analytics data for the Admin Dashboard.
 */
public class AnalyticsDAO {

    // ==================== USER METRICS ====================

    /**
     * Get total number of users.
     */
    public int getTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get number of active users (users with is_active = true).
     */
    public int getActiveUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE is_active = TRUE";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get signups count for a specific period.
     * @param days Number of days to look back (1 = today only, 7 = last 7 days, 30 = last 30 days)
     */
    public int getSignupsInPeriod(int days) throws SQLException {
        String sql;
        if (days == 1) {
            // For today: count users created today (from start of today)
            sql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()";
        } else {
            // For other periods: count users from the start of the day N days ago until now
            sql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";
        }
        
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (days != 1) {
                ps.setInt(1, days - 1); // -1 because we want to include today
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    /**
     * Get total signups for today only.
     */
    public int getTodaySignups() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) = CURDATE()";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Get signups for this week (from Monday of current week).
     */
    public int getThisWeekSignups() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE YEARWEEK(created_at, 1) = YEARWEEK(CURDATE(), 1)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Get signups for this month.
     */
    public int getThisMonthSignups() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get daily signup trend for the last N days.
     * Returns a list of maps with 'date' and 'count' keys.
     */
    public List<Map<String, Object>> getSignupTrend(int days) throws SQLException {
        List<Map<String, Object>> trend = new ArrayList<>();
        String sql = "SELECT DATE(created_at) as signup_date, COUNT(*) as count " +
                     "FROM users " +
                     "WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY DATE(created_at) " +
                     "ORDER BY signup_date";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", rs.getDate("signup_date").toLocalDate());
                    point.put("count", rs.getInt("count"));
                    trend.add(point);
                }
            }
        }
        return trend;
    }

    // ==================== INTERVIEW/COURSE METRICS ====================

    /**
     * Get total number of completed interviews (questions answered).
     */
    public int getTotalInterviewsCompleted() throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE status = 'COMPLETED'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get total number of courses created.
     */
    public int getTotalCourses() throws SQLException {
        String sql = "SELECT COUNT(*) FROM generated_courses";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get average score (XP) per user.
     */
    public double getAverageScore() throws SQLException {
        String sql = "SELECT AVG(total_xp) FROM (SELECT user_id, SUM(xp) as total_xp FROM progress GROUP BY user_id) as user_scores";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    /**
     * Get interview type distribution from onboarding_data.
     * Returns map of interview_type -> count.
     */
    public Map<String, Integer> getInterviewTypeDistribution() throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT interview_type, COUNT(*) as count FROM onboarding_data GROUP BY interview_type";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                distribution.put(rs.getString("interview_type"), rs.getInt("count"));
            }
        }
        return distribution;
    }

    /**
     * Get language distribution from onboarding_data.
     * Returns map of language -> count.
     */
    public Map<String, Integer> getLanguageDistribution() throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT language, COUNT(*) as count FROM onboarding_data GROUP BY language";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                distribution.put(rs.getString("language"), rs.getInt("count"));
            }
        }
        return distribution;
    }

    /**
     * Get most common interview type.
     */
    public String getMostCommonInterviewType() throws SQLException {
        String sql = "SELECT interview_type, COUNT(*) as count FROM onboarding_data " +
                     "GROUP BY interview_type ORDER BY count DESC LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("interview_type");
            }
        }
        return "N/A";
    }

    /**
     * Get most selected language.
     */
    public String getMostSelectedLanguage() throws SQLException {
        String sql = "SELECT language, COUNT(*) as count FROM onboarding_data " +
                     "GROUP BY language ORDER BY count DESC LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("language");
            }
        }
        return "N/A";
    }

    // ==================== ACTIVITY METRICS ====================

    /**
     * Get daily active users (users who have progress entries in the last N days).
     */
    public int getDailyActiveUsers(int days) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT user_id) FROM progress WHERE last_updated >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Get usage trend (progress entries per day) for the last N days.
     */
    public List<Map<String, Object>> getUsageTrend(int days) throws SQLException {
        List<Map<String, Object>> trend = new ArrayList<>();
        String sql = "SELECT DATE(last_updated) as activity_date, COUNT(*) as count " +
                     "FROM progress " +
                     "WHERE last_updated >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY DATE(last_updated) " +
                     "ORDER BY activity_date";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", rs.getDate("activity_date").toLocalDate());
                    point.put("count", rs.getInt("count"));
                    trend.add(point);
                }
            }
        }
        return trend;
    }

    /**
     * Get total XP earned in the last N days.
     */
    public int getTotalXPInPeriod(int days) throws SQLException {
        String sql = "SELECT COALESCE(SUM(xp), 0) FROM progress WHERE last_updated >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Get chapters completed count.
     */
    public int getCompletedChapters() throws SQLException {
        String sql = "SELECT COUNT(*) FROM chapters WHERE status = 'COMPLETED'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get chapters in progress count.
     */
    public int getInProgressChapters() throws SQLException {
        String sql = "SELECT COUNT(*) FROM chapters WHERE status = 'IN_PROGRESS'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // ==================== FEEDBACK METRICS ====================

    /**
     * Get total feedback count.
     */
    public int getTotalFeedback() throws SQLException {
        String sql = "SELECT COUNT(*) FROM feedback";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get recent feedback messages.
     */
    public List<Map<String, Object>> getRecentFeedback(int limit) throws SQLException {
        List<Map<String, Object>> feedbackList = new ArrayList<>();
        String sql = "SELECT f.id, f.message, f.created_at, u.username " +
                     "FROM feedback f " +
                     "LEFT JOIN users u ON f.user_id = u.id " +
                     "ORDER BY f.created_at DESC LIMIT ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("message", rs.getString("message"));
                    item.put("created_at", rs.getTimestamp("created_at"));
                    item.put("username", rs.getString("username"));
                    feedbackList.add(item);
                }
            }
        }
        return feedbackList;
    }
}
