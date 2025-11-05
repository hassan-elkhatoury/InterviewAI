package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.interviewai.model.OnboardingData;

/**
 * DAO for onboarding_data table.
 * TODO: Create onboarding_data table in schema.sql
 */
public class OnboardingDAO {
    
    /**
     * Saves or updates onboarding data for a user.
     * TODO: Implement after creating onboarding_data table.
     */
    public boolean saveOnboardingData(OnboardingData data) throws SQLException {
        String sql = "INSERT INTO onboarding_data (user_id, interview_type, language, timeline, context, cv_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE interview_type = ?, language = ?, timeline = ?, context = ?, cv_path = ?";
        
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, data.getUserId());
            ps.setString(2, data.getInterviewType());
            ps.setString(3, data.getLanguage());
            ps.setString(4, data.getTimeline());
            ps.setString(5, data.getContext());
            ps.setString(6, data.getCvPath());
            // For ON DUPLICATE KEY UPDATE
            ps.setString(7, data.getInterviewType());
            ps.setString(8, data.getLanguage());
            ps.setString(9, data.getTimeline());
            ps.setString(10, data.getContext());
            ps.setString(11, data.getCvPath());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Retrieves onboarding data for a user.
     */
    public OnboardingData getByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM onboarding_data WHERE user_id = ?";
        
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OnboardingData data = new OnboardingData();
                    data.setUserId(rs.getInt("user_id"));
                    data.setInterviewType(rs.getString("interview_type"));
                    data.setLanguage(rs.getString("language"));
                    data.setTimeline(rs.getString("timeline"));
                    data.setContext(rs.getString("context"));
                    data.setCvPath(rs.getString("cv_path"));
                    return data;
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if user has completed onboarding.
     */
    public boolean hasCompletedOnboarding(int userId) throws SQLException {
        return getByUserId(userId) != null;
    }
}
