package com.interviewai.dao;

import com.interviewai.model.GeneratedCourse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * Get all interview prep records with course details for a user.
     * Returns a list with both technical and soft-skills courses.
     * 
     * @param userId User ID
     * @return List of interview prep pairs (technical + soft-skills)
     */
    public List<InterviewPrepRecord> getAllInterviewPreps(int userId) {
        List<InterviewPrepRecord> records = new ArrayList<>();
        String sql = "SELECT ip.id, ip.technical_course_id, ip.softskills_course_id, " +
                     "tc.course_title as technical_title, sc.course_title as softskills_title, " +
                     "ip.created_at " +
                     "FROM interview_prep ip " +
                     "LEFT JOIN generated_courses tc ON ip.technical_course_id = tc.id " +
                     "LEFT JOIN generated_courses sc ON ip.softskills_course_id = sc.id " +
                     "WHERE ip.user_id = ? " +
                     "ORDER BY ip.created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InterviewPrepRecord record = new InterviewPrepRecord();
                    record.id = rs.getInt("id");
                    record.technicalCourseId = rs.getInt("technical_course_id");
                    record.softskillsCourseId = rs.getInt("softskills_course_id");
                    record.technicalTitle = rs.getString("technical_title");
                    record.softskillsTitle = rs.getString("softskills_title");
                    record.createdAt = rs.getTimestamp("created_at");
                    records.add(record);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to get interview prep records: " + e.getMessage());
        }
        
        return records;
    }
    
    /**
     * Inner class to hold interview prep record data.
     */
    public static class InterviewPrepRecord {
        public int id;
        public int technicalCourseId;
        public int softskillsCourseId;
        public String technicalTitle;
        public String softskillsTitle;
        public java.sql.Timestamp createdAt;
    }
}
