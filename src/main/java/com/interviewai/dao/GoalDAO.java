package com.interviewai.dao;

import com.interviewai.model.UserGoal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing user goals in the database
 */
public class GoalDAO {
    
    private final ProgressDAO progressDAO = new ProgressDAO();

    /**
     * Get all active goals for a user
     */
    public List<UserGoal> getUserGoals(int userId) throws SQLException {
        List<UserGoal> goals = new ArrayList<>();
        String query = "SELECT * FROM user_goals WHERE user_id = ? AND is_active = 1 ORDER BY created_at ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                UserGoal goal = new UserGoal();
                goal.setId(rs.getInt("id"));
                goal.setUserId(rs.getInt("user_id"));
                goal.setGoalName(rs.getString("goal_name"));
                goal.setGoalType(rs.getString("goal_type"));
                goal.setTargetValue(rs.getInt("target_value"));
                goal.setCurrentValue(rs.getInt("current_value"));
                goal.setStartValue(rs.getInt("start_value"));
                goal.setActive(rs.getBoolean("is_active"));
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    goal.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    goal.setUpdatedAt(updatedAt.toLocalDateTime());
                }
                
                goals.add(goal);
            }
        }
        
        return goals;
    }

    /**
     * Create a new goal
     */
    public void createGoal(UserGoal goal) throws SQLException {
        // Calculate start value based on current stats if goal is incremental
        int startValue = 0;
        if (isIncremental(goal.getGoalType())) {
            startValue = calculateTotalMetric(goal.getUserId(), goal.getGoalType());
        }
        goal.setStartValue(startValue);

        String query = "INSERT INTO user_goals (user_id, goal_name, goal_type, target_value, start_value, current_value, is_active) " +
                       "VALUES (?, ?, ?, ?, ?, 0, 1)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, goal.getUserId());
            stmt.setString(2, goal.getGoalName());
            stmt.setString(3, goal.getGoalType());
            stmt.setInt(4, goal.getTargetValue());
            stmt.setInt(5, startValue);
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                goal.setId(rs.getInt(1));
            }
        }
    }

    /**
     * Update an existing goal
     */
    public void updateGoal(UserGoal goal) throws SQLException {
        String query = "UPDATE user_goals SET goal_name = ?, goal_type = ?, target_value = ?, updated_at = NOW() " +
                       "WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, goal.getGoalName());
            stmt.setString(2, goal.getGoalType());
            stmt.setInt(3, goal.getTargetValue());
            stmt.setInt(4, goal.getId());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a goal (soft delete - mark as inactive)
     */
    public void deleteGoal(int goalId) throws SQLException {
        String query = "UPDATE user_goals SET is_active = 0, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, goalId);
            stmt.executeUpdate();
        }
    }

    /**
     * Update goal progress based on current user stats
     */
    public void updateGoalProgress(int userId) throws SQLException {
        List<UserGoal> goals = getUserGoals(userId);
        
        for (UserGoal goal : goals) {
            int totalMetric = calculateTotalMetric(userId, goal.getGoalType());
            
            // Calculate current value (Total - Start) for incremental, or just Total for absolute
            int currentValue = totalMetric;
            if (isIncremental(goal.getGoalType())) {
                currentValue = Math.max(0, totalMetric - goal.getStartValue());
            }

            // Cap at target value? No, let them exceed it.

            String query = "UPDATE user_goals SET current_value = ?, updated_at = NOW() WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, currentValue);
                stmt.setInt(2, goal.getId());
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Calculate current TOTAL metric for a goal type
     */
    private int calculateTotalMetric(int userId, String goalType) throws SQLException {
        switch (goalType) {
            case "QUESTIONS":
                return progressDAO.getTotalQuestionsAnswered(userId);
            case "XP":
                return progressDAO.getTotalXPForUser(userId);
            case "CHAPTERS":
                return progressDAO.getChaptersCompletedThisMonth(userId);
            case "TIME":
                return progressDAO.getEstimatedTimeSpent(userId);
            case "COURSES":
                return progressDAO.getTotalCoursesEnrolled(userId);
            case "STREAK":
                return progressDAO.calculateUserStreak(userId);
            default:
                return 0;
        }
    }
    
    private boolean isIncremental(String goalType) {
        // STREAK is absolute (e.g. Reach 30 day streak)
        // Others are incremental (e.g. Answer 20 questions from now)
        return !goalType.equals("STREAK");
    }

    /**
     * Get goals with updated progress
     */
    public List<UserGoal> getUserGoalsWithProgress(int userId) throws SQLException {
        // First update all goal progress
        updateGoalProgress(userId);
        
        // Then fetch and return updated goals
        return getUserGoals(userId);
    }
}
