package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.interviewai.model.UserManagementDTO;

/**
 * DAO for admin user management operations.
 * Provides comprehensive user data retrieval and management capabilities.
 */
public class UserManagementDAO {
    
    private RoleDAO roleDAO = new RoleDAO();
    
    /**
     * Get all users with comprehensive details for admin management.
     * Includes onboarding data, progress stats, and activity information.
     */
    public List<UserManagementDTO> getAllUsersWithDetails() throws SQLException {
        List<UserManagementDTO> users = new ArrayList<>();
        
        String sql = """
            SELECT 
                u.id, u.username, u.email, u.is_active, u.two_factor_enabled, u.created_at,
                COALESCE(r.name, 'CANDIDATE') as role_name,
                o.interview_type, o.language, o.timeline, o.context, o.cv_path,
                COALESCE(p.total_xp, 0) as total_xp,
                COALESCE(p.last_activity, u.created_at) as last_active,
                COALESCE(c.completed_lessons, 0) as completed_lessons,
                COALESCE(gc.enrolled_courses, 0) as enrolled_courses
            FROM users u
            LEFT JOIN user_roles ur ON u.id = ur.user_id
            LEFT JOIN roles r ON ur.role_id = r.id
            LEFT JOIN onboarding_data o ON u.id = o.user_id
            LEFT JOIN (
                SELECT user_id, SUM(xp) as total_xp, MAX(last_updated) as last_activity
                FROM progress
                GROUP BY user_id
            ) p ON u.id = p.user_id
            LEFT JOIN (
                SELECT gc.user_id, COUNT(DISTINCT ch.id) as completed_lessons
                FROM generated_courses gc
                JOIN chapters ch ON gc.id = ch.course_id AND ch.status = 'COMPLETED'
                GROUP BY gc.user_id
            ) c ON u.id = c.user_id
            LEFT JOIN (
                SELECT user_id, COUNT(DISTINCT id) as enrolled_courses
                FROM generated_courses
                GROUP BY user_id
            ) gc ON u.id = gc.user_id
            ORDER BY u.created_at DESC
        """;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                UserManagementDTO user = mapResultSetToDTO(rs);
                users.add(user);
            }
        }
        
        return users;
    }
    
    /**
     * Search users by name or email with optional filters.
     */
    public List<UserManagementDTO> searchUsers(String searchTerm, String interviewType, 
            String language, String status, String progressLevel) throws SQLException {
        
        List<UserManagementDTO> users = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT 
                u.id, u.username, u.email, u.is_active, u.two_factor_enabled, u.created_at,
                COALESCE(r.name, 'CANDIDATE') as role_name,
                o.interview_type, o.language, o.timeline, o.context, o.cv_path,
                COALESCE(p.total_xp, 0) as total_xp,
                COALESCE(p.last_activity, u.created_at) as last_active,
                COALESCE(c.completed_lessons, 0) as completed_lessons,
                COALESCE(gc.enrolled_courses, 0) as enrolled_courses
            FROM users u
            LEFT JOIN user_roles ur ON u.id = ur.user_id
            LEFT JOIN roles r ON ur.role_id = r.id
            LEFT JOIN onboarding_data o ON u.id = o.user_id
            LEFT JOIN (
                SELECT user_id, SUM(xp) as total_xp, MAX(last_updated) as last_activity
                FROM progress
                GROUP BY user_id
            ) p ON u.id = p.user_id
            LEFT JOIN (
                SELECT gc.user_id, COUNT(DISTINCT ch.id) as completed_lessons
                FROM generated_courses gc
                JOIN chapters ch ON gc.id = ch.course_id AND ch.status = 'COMPLETED'
                GROUP BY gc.user_id
            ) c ON u.id = c.user_id
            LEFT JOIN (
                SELECT user_id, COUNT(DISTINCT id) as enrolled_courses
                FROM generated_courses
                GROUP BY user_id
            ) gc ON u.id = gc.user_id
            WHERE 1=1
        """);
        
        List<Object> params = new ArrayList<>();
        
        // Search term filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (u.username LIKE ? OR u.email LIKE ?)");
            String term = "%" + searchTerm.trim() + "%";
            params.add(term);
            params.add(term);
        }
        
        // Interview type filter
        if (interviewType != null && !interviewType.isEmpty() && !interviewType.equals("All Types")) {
            sql.append(" AND o.interview_type = ?");
            params.add(interviewType);
        }
        
        // Language filter
        if (language != null && !language.isEmpty() && !language.equals("All Languages")) {
            sql.append(" AND o.language = ?");
            params.add(language);
        }
        
        // Status filter
        if (status != null && !status.isEmpty() && !status.equals("All Status")) {
            sql.append(" AND u.is_active = ?");
            params.add(status.equals("Active") ? 1 : 0);
        }
        
        // Progress level filter
        if (progressLevel != null && !progressLevel.isEmpty() && !progressLevel.equals("All Levels")) {
            switch (progressLevel) {
                case "Beginner (0-100 XP)":
                    sql.append(" AND COALESCE(p.total_xp, 0) BETWEEN 0 AND 100");
                    break;
                case "Intermediate (100-500 XP)":
                    sql.append(" AND COALESCE(p.total_xp, 0) BETWEEN 101 AND 500");
                    break;
                case "Advanced (500+ XP)":
                    sql.append(" AND COALESCE(p.total_xp, 0) > 500");
                    break;
            }
        }
        
        sql.append(" ORDER BY u.created_at DESC");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UserManagementDTO user = mapResultSetToDTO(rs);
                    users.add(user);
                }
            }
        }
        
        return users;
    }
    
    /**
     * Get detailed user profile by ID for admin view.
     */
    public UserManagementDTO getUserById(int userId) throws SQLException {
        String sql = """
            SELECT 
                u.id, u.username, u.email, u.is_active, u.two_factor_enabled, u.created_at,
                COALESCE(r.name, 'CANDIDATE') as role_name,
                o.interview_type, o.language, o.timeline, o.context, o.cv_path,
                COALESCE(p.total_xp, 0) as total_xp,
                COALESCE(p.last_activity, u.created_at) as last_active,
                COALESCE(c.completed_lessons, 0) as completed_lessons,
                COALESCE(gc.enrolled_courses, 0) as enrolled_courses
            FROM users u
            LEFT JOIN user_roles ur ON u.id = ur.user_id
            LEFT JOIN roles r ON ur.role_id = r.id
            LEFT JOIN onboarding_data o ON u.id = o.user_id
            LEFT JOIN (
                SELECT user_id, SUM(xp) as total_xp, MAX(last_updated) as last_activity
                FROM progress
                GROUP BY user_id
            ) p ON u.id = p.user_id
            LEFT JOIN (
                SELECT gc.user_id, COUNT(DISTINCT ch.id) as completed_lessons
                FROM generated_courses gc
                JOIN chapters ch ON gc.id = ch.course_id AND ch.status = 'COMPLETED'
                GROUP BY gc.user_id
            ) c ON u.id = c.user_id
            LEFT JOIN (
                SELECT user_id, COUNT(DISTINCT id) as enrolled_courses
                FROM generated_courses
                GROUP BY user_id
            ) gc ON u.id = gc.user_id
            WHERE u.id = ?
        """;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDTO(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Suspend a user account.
     */
    public boolean suspendUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = 0 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() == 1;
        }
    }
    
    /**
     * Activate a user account.
     */
    public boolean activateUser(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() == 1;
        }
    }
    
    /**
     * Reset user's XP progress.
     */
    public boolean resetUserXP(int userId) throws SQLException {
        String sql = "UPDATE progress SET xp = 0 WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            return true;
        }
    }
    
    /**
     * Reset user's entire progress (XP + chapter status).
     */
    public boolean resetUserProgress(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Reset XP
                String xpSql = "UPDATE progress SET xp = 0 WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(xpSql)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                
                // Reset chapter status for user's courses
                String chapterSql = """
                    UPDATE chapters SET status = 'NOT_STARTED', completed_at = NULL
                    WHERE course_id IN (SELECT id FROM generated_courses WHERE user_id = ?)
                """;
                try (PreparedStatement stmt = conn.prepareStatement(chapterSql)) {
                    stmt.setInt(1, userId);
                    stmt.executeUpdate();
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
    /**
     * Update user's email.
     */
    public boolean updateUserEmail(int userId, String newEmail) throws SQLException {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newEmail);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() == 1;
        }
    }
    
    /**
     * Update user's role.
     */
    public boolean updateUserRole(int userId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() == 1;
        }
    }
    
    /**
     * Update user's username.
     */
    public boolean updateUsername(int userId, String newUsername) throws SQLException {
        String sql = "UPDATE users SET username = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUsername);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() == 1;
        }
    }
    
    /**
     * Get all distinct interview types for filter dropdown.
     */
    public List<String> getDistinctInterviewTypes() throws SQLException {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT interview_type FROM onboarding_data WHERE interview_type IS NOT NULL ORDER BY interview_type";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                types.add(rs.getString("interview_type"));
            }
        }
        return types;
    }
    
    /**
     * Get all distinct languages for filter dropdown.
     */
    public List<String> getDistinctLanguages() throws SQLException {
        List<String> languages = new ArrayList<>();
        String sql = "SELECT DISTINCT language FROM onboarding_data WHERE language IS NOT NULL ORDER BY language";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                languages.add(rs.getString("language"));
            }
        }
        return languages;
    }
    
    /**
     * Get user's badges (placeholder - actual badge table may be needed)
     */
    public List<String> getUserBadges(int userId) throws SQLException {
        // This is a placeholder - in a real implementation, you'd have a badges table
        // For now, return based on XP milestones
        List<String> badges = new ArrayList<>();
        
        String sql = "SELECT COALESCE(SUM(xp), 0) as total_xp FROM progress WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int xp = rs.getInt("total_xp");
                    if (xp >= 10) badges.add("First Steps");
                    if (xp >= 100) badges.add("Learner");
                    if (xp >= 500) badges.add("Dedicated");
                    if (xp >= 1000) badges.add("Expert");
                }
            }
        }
        
        return badges;
    }
    
    /**
     * Count total users.
     */
    public int getTotalUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Helper method to map ResultSet to UserManagementDTO.
     */
    private UserManagementDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        UserManagementDTO user = new UserManagementDTO();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));
        user.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setRole(rs.getString("role_name"));
        
        // Onboarding data
        user.setInterviewType(rs.getString("interview_type"));
        user.setLanguage(rs.getString("language"));
        user.setTimeline(rs.getString("timeline"));
        user.setContext(rs.getString("context"));
        user.setCvPath(rs.getString("cv_path"));
        
        // Progress data
        user.setTotalXP(rs.getInt("total_xp"));
        user.setLastActive(rs.getTimestamp("last_active"));
        user.setCompletedLessons(rs.getInt("completed_lessons"));
        user.setEnrolledCourses(rs.getInt("enrolled_courses"));
        
        return user;
    }
}
