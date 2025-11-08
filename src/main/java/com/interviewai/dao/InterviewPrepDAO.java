package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO for managing interview_prep table.
 * Links users with their generated technical and soft-skills courses.
 */
public class InterviewPrepDAO {
    
    /**
     * Insert a new interview prep record linking user to both courses.
     * 
     * @param userId User ID
     * @param technicalCourseId ID of the generated technical course
     * @param softskillsCourseId ID of the generated soft-skills course
     * @return true if successful, false otherwise
     */
    public boolean insertInterviewPrep(int userId, int technicalCourseId, int softskillsCourseId) {
        String sql = "INSERT INTO interview_prep (user_id, technical_course_id, softskills_course_id, created_at) " +
                     "VALUES (?, ?, ?, NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, technicalCourseId);
            ps.setInt(3, softskillsCourseId);
            
            int rowsAffected = ps.executeUpdate();
            System.out.println("✓ Interview prep record created: user=" + userId + 
                             ", technical=" + technicalCourseId + 
                             ", softskills=" + softskillsCourseId);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to insert interview prep record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get the technical course ID for a user's interview prep.
     * 
     * @param userId User ID
     * @return Technical course ID, or -1 if not found
     */
    public int getTechnicalCourseId(int userId) {
        String sql = "SELECT technical_course_id FROM interview_prep WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("technical_course_id");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to get technical course ID: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Get the soft-skills course ID for a user's interview prep.
     * 
     * @param userId User ID
     * @return Soft-skills course ID, or -1 if not found
     */
    public int getSoftskillsCourseId(int userId) {
        String sql = "SELECT softskills_course_id FROM interview_prep WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("softskills_course_id");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to get soft-skills course ID: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Check if a user has an interview prep record.
     * 
     * @param userId User ID
     * @return true if record exists, false otherwise
     */
    public boolean hasInterviewPrep(int userId) {
        String sql = "SELECT COUNT(*) FROM interview_prep WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to check interview prep existence: " + e.getMessage());
        }
        
        return false;
    }
}
