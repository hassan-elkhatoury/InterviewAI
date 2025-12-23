package com.interviewai.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.interviewai.dao.AnalyticsDAO;

/**
 * Service layer for aggregating analytics data for the Admin Dashboard.
 */
public class AnalyticsService {

    private final AnalyticsDAO analyticsDAO;

    public AnalyticsService() {
        this.analyticsDAO = new AnalyticsDAO();
    }

    /**
     * Get all dashboard metrics in a single call.
     * Returns a map containing all KPIs and data for charts.
     */
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // User Metrics
            metrics.put("totalUsers", analyticsDAO.getTotalUsers());
            metrics.put("activeUsers", analyticsDAO.getActiveUsers());
            metrics.put("dailySignups", analyticsDAO.getTodaySignups());
            metrics.put("weeklySignups", analyticsDAO.getThisWeekSignups());
            metrics.put("monthlySignups", analyticsDAO.getThisMonthSignups());

            // Activity Metrics
            metrics.put("dailyActiveUsers", analyticsDAO.getDailyActiveUsers(1));
            metrics.put("weeklyActiveUsers", analyticsDAO.getDailyActiveUsers(7));

            // Course/Interview Metrics
            metrics.put("totalCourses", analyticsDAO.getTotalCourses());
            metrics.put("interviewsCompleted", analyticsDAO.getTotalInterviewsCompleted());
            metrics.put("averageScore", analyticsDAO.getAverageScore());
            metrics.put("completedChapters", analyticsDAO.getCompletedChapters());
            metrics.put("inProgressChapters", analyticsDAO.getInProgressChapters());

            // Distribution Data (for pie charts)
            metrics.put("interviewTypeDistribution", analyticsDAO.getInterviewTypeDistribution());
            metrics.put("languageDistribution", analyticsDAO.getLanguageDistribution());
            metrics.put("mostCommonInterviewType", analyticsDAO.getMostCommonInterviewType());
            metrics.put("mostSelectedLanguage", analyticsDAO.getMostSelectedLanguage());

            // Trend Data (for line charts)
            metrics.put("signupTrend", analyticsDAO.getSignupTrend(30));
            metrics.put("usageTrend", analyticsDAO.getUsageTrend(30));

            // XP Metrics
            metrics.put("totalXPToday", analyticsDAO.getTotalXPInPeriod(1));
            metrics.put("totalXPWeek", analyticsDAO.getTotalXPInPeriod(7));

            // Feedback
            metrics.put("totalFeedback", analyticsDAO.getTotalFeedback());
            metrics.put("recentFeedback", analyticsDAO.getRecentFeedback(5));

            // System Health (placeholder - these would typically come from monitoring)
            metrics.put("systemHealth", getSystemHealthMetrics());

        } catch (SQLException e) {
            e.printStackTrace();
            // Return partial metrics or defaults
            metrics.put("error", "Failed to load some metrics: " + e.getMessage());
        }

        return metrics;
    }

    /**
     * Get user-related metrics only.
     */
    public Map<String, Object> getUserMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        try {
            metrics.put("totalUsers", analyticsDAO.getTotalUsers());
            metrics.put("activeUsers", analyticsDAO.getActiveUsers());
            metrics.put("dailySignups", analyticsDAO.getTodaySignups());
            metrics.put("weeklySignups", analyticsDAO.getThisWeekSignups());
            metrics.put("monthlySignups", analyticsDAO.getThisMonthSignups());
            metrics.put("signupTrend", analyticsDAO.getSignupTrend(30));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return metrics;
    }

    /**
     * Get course/interview metrics only.
     */
    public Map<String, Object> getCourseMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        try {
            metrics.put("totalCourses", analyticsDAO.getTotalCourses());
            metrics.put("interviewsCompleted", analyticsDAO.getTotalInterviewsCompleted());
            metrics.put("averageScore", analyticsDAO.getAverageScore());
            metrics.put("interviewTypeDistribution", analyticsDAO.getInterviewTypeDistribution());
            metrics.put("languageDistribution", analyticsDAO.getLanguageDistribution());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return metrics;
    }

    /**
     * Get system health metrics.
     * In a real application, these would come from monitoring tools.
     */
    private Map<String, Object> getSystemHealthMetrics() {
        Map<String, Object> health = new HashMap<>();
        
        // These are placeholder values - in production, you'd get these from actual monitoring
        health.put("aiResponseTime", "120ms"); // Would come from AI service monitoring
        health.put("aiFailureRate", "0.5%"); // Would come from error tracking
        health.put("apiUptime", "99.9%"); // Would come from uptime monitoring
        health.put("dbConnectionStatus", checkDatabaseHealth());
        health.put("lastHealthCheck", java.time.LocalDateTime.now().toString());

        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        health.put("memoryUsage", usedMemory + "MB / " + maxMemory + "MB");
        health.put("memoryPercent", (int) ((usedMemory * 100) / maxMemory));

        return health;
    }

    /**
     * Check database connection health.
     */
    private String checkDatabaseHealth() {
        try {
            // Simple query to check DB is responsive
            analyticsDAO.getTotalUsers();
            return "Healthy";
        } catch (SQLException e) {
            return "Unhealthy: " + e.getMessage();
        }
    }

    /**
     * Get distribution data for pie charts.
     */
    public Map<String, Map<String, Integer>> getDistributions() {
        Map<String, Map<String, Integer>> distributions = new HashMap<>();
        try {
            distributions.put("interviewTypes", analyticsDAO.getInterviewTypeDistribution());
            distributions.put("languages", analyticsDAO.getLanguageDistribution());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return distributions;
    }

    /**
     * Get trend data for line charts.
     */
    public Map<String, List<Map<String, Object>>> getTrends(int days) {
        Map<String, List<Map<String, Object>>> trends = new HashMap<>();
        try {
            trends.put("signups", analyticsDAO.getSignupTrend(days));
            trends.put("usage", analyticsDAO.getUsageTrend(days));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trends;
    }
}
