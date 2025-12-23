package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.interviewai.model.CourseManagementDTO;

/**
 * DAO for admin course management operations.
 * Provides methods to list, filter, and manage courses across all users.
 */
public class CourseManagementDAO {

    /**
     * Get all courses with user information and statistics.
     * @return List of CourseManagementDTO objects
     */
    public List<CourseManagementDTO> getAllCourses() {
        List<CourseManagementDTO> courses = new ArrayList<>();
        String sql = """
            SELECT 
                gc.id,
                gc.user_id,
                u.username,
                u.email,
                gc.course_title,
                gc.status,
                gc.created_at,
                (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id) as chapters_count,
                (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id AND c.status = 'COMPLETED') as completed_chapters,
                (SELECT COUNT(*) FROM questions q 
                    INNER JOIN chapters c ON q.chapter_id = c.id 
                    WHERE c.course_id = gc.id) as questions_count
            FROM generated_courses gc
            INNER JOIN users u ON gc.user_id = u.id
            ORDER BY gc.created_at DESC
            """;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                courses.add(mapResultSetToDTO(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all courses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return courses;
    }

    /**
     * Search courses by username, email, or course title.
     * @param searchTerm The search term
     * @return Filtered list of courses
     */
    public List<CourseManagementDTO> searchCourses(String searchTerm) {
        List<CourseManagementDTO> courses = new ArrayList<>();
        String sql = """
            SELECT 
                gc.id,
                gc.user_id,
                u.username,
                u.email,
                gc.course_title,
                gc.status,
                gc.created_at,
                (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id) as chapters_count,
                (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id AND c.status = 'COMPLETED') as completed_chapters,
                (SELECT COUNT(*) FROM questions q 
                    INNER JOIN chapters c ON q.chapter_id = c.id 
                    WHERE c.course_id = gc.id) as questions_count
            FROM generated_courses gc
            INNER JOIN users u ON gc.user_id = u.id
            WHERE u.username LIKE ? OR u.email LIKE ? OR gc.course_title LIKE ?
            ORDER BY gc.created_at DESC
            """;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + searchTerm + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToDTO(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching courses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return courses;
    }

    /**
     * Filter courses by status.
     * @param status The status to filter by (ACTIVE, COMPLETE, ARCHIVED, etc.)
     * @return Filtered list of courses
     */
    public List<CourseManagementDTO> getCoursesByStatus(String status) {
        List<CourseManagementDTO> courses = new ArrayList<>();
        String sql = """
            SELECT 
                gc.id,
                gc.user_id,
                u.username,
                u.email,
                gc.course_title,
                gc.status,
                gc.created_at,
                (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id) as chapters_count,
                (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id AND c.status = 'COMPLETED') as completed_chapters,
                (SELECT COUNT(*) FROM questions q 
                    INNER JOIN chapters c ON q.chapter_id = c.id 
                    WHERE c.course_id = gc.id) as questions_count
            FROM generated_courses gc
            INNER JOIN users u ON gc.user_id = u.id
            WHERE gc.status = ?
            ORDER BY gc.created_at DESC
            """;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToDTO(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering courses by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return courses;
    }

    /**
     * Get courses for a specific user.
     * @param userId The user ID
     * @return List of courses for that user
     */
    public List<CourseManagementDTO> getCoursesByUser(int userId) {
        List<CourseManagementDTO> courses = new ArrayList<>();
        String sql = """
            SELECT 
                gc.id,
                gc.user_id,
                u.username,
                u.email,
                gc.course_title,
                gc.status,
                gc.created_at,
                (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id) as chapters_count,
                (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id AND c.status = 'COMPLETED') as completed_chapters,
                (SELECT COUNT(*) FROM questions q 
                    INNER JOIN chapters c ON q.chapter_id = c.id 
                    WHERE c.course_id = gc.id) as questions_count
            FROM generated_courses gc
            INNER JOIN users u ON gc.user_id = u.id
            WHERE gc.user_id = ?
            ORDER BY gc.created_at DESC
            """;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToDTO(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching courses by user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return courses;
    }

    /**
     * Update course status.
     * @param courseId The course ID
     * @param newStatus The new status
     * @return true if update successful
     */
    public boolean updateCourseStatus(int courseId, String newStatus) {
        String sql = "UPDATE generated_courses SET status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, courseId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a course and all its related data (chapters, questions, choices).
     * @param courseId The course ID to delete
     * @return true if deletion successful
     */
    public boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM generated_courses WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get course statistics for dashboard display.
     * @return Array with [totalCourses, activeCourses, completedCourses, totalChapters, totalQuestions]
     */
    public int[] getCourseStatistics() {
        int[] stats = new int[6]; // [total, active, completed, chapters, questions, avgCompletion]
        
        String sql = """
            SELECT 
                COUNT(*) as total_courses,
                SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_courses,
                SUM(CASE WHEN status IN ('COMPLETE', 'COMPLETED') THEN 1 ELSE 0 END) as completed_courses
            FROM generated_courses
            """;
        
        String chaptersSql = "SELECT COUNT(*) FROM chapters";
        String questionsSql = "SELECT COUNT(*) FROM questions";
        
        // Calculate average completion
        String avgCompletionSql = """
            SELECT COALESCE(AVG(
                CASE 
                    WHEN total_chapters > 0 THEN (completed_chapters * 100.0 / total_chapters)
                    ELSE 0 
                END
            ), 0) as avg_completion
            FROM (
                SELECT 
                    gc.id,
                    (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id) as total_chapters,
                    (SELECT COUNT(*) FROM chapters c WHERE c.course_id = gc.id AND c.status = 'COMPLETED') as completed_chapters
                FROM generated_courses gc
            ) course_stats
            """;
        
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats[0] = rs.getInt("total_courses");
                    stats[1] = rs.getInt("active_courses");
                    stats[2] = rs.getInt("completed_courses");
                }
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(chaptersSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats[3] = rs.getInt(1);
                }
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(questionsSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats[4] = rs.getInt(1);
                }
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(avgCompletionSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats[5] = (int) Math.round(rs.getDouble("avg_completion"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * Get unique course statuses for filter dropdown.
     * @return List of distinct status values
     */
    public List<String> getDistinctStatuses() {
        List<String> statuses = new ArrayList<>();
        String sql = "SELECT DISTINCT status FROM generated_courses WHERE status IS NOT NULL ORDER BY status";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String status = rs.getString("status");
                if (status != null && !status.isEmpty()) {
                    statuses.add(status);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching distinct statuses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return statuses;
    }

    private CourseManagementDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        CourseManagementDTO dto = new CourseManagementDTO();
        dto.setId(rs.getInt("id"));
        dto.setUserId(rs.getInt("user_id"));
        dto.setUsername(rs.getString("username"));
        dto.setUserEmail(rs.getString("email"));
        dto.setCourseTitle(rs.getString("course_title"));
        dto.setStatus(rs.getString("status"));
        
        java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            dto.setCreatedAt(timestamp.toLocalDateTime());
        }
        
        dto.setChaptersCount(rs.getInt("chapters_count"));
        dto.setCompletedChapters(rs.getInt("completed_chapters"));
        dto.setQuestionsCount(rs.getInt("questions_count"));
        
        return dto;
    }
}
