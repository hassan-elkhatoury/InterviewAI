package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import com.interviewai.model.Role;
import com.interviewai.model.User;

/**
 * Basic JDBC-backed DAO for users.
 */


public class UserDAO {

    private RoleDAO roleDAO = new RoleDAO();

    /**
     * Creates a user if username is not taken. Password is hashed with BCrypt.
     * Returns true if a row was inserted.
     */
    public boolean createUser(String username, String email, String rawPassword, String roleName) throws SQLException {
        String sqlCheck = "SELECT id FROM users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement psCheck = c.prepareStatement(sqlCheck)) {
            psCheck.setString(1, username);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) return false; // username taken
            }
        }

        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        int userId = -1;

        try (Connection c = DBConnection.getConnection()) {
            // Insert user
            String sqlInsert = "INSERT INTO users (username, email, password_hash, is_active, two_factor_enabled) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, hash);
                ps.setBoolean(4, true); // Default active
                ps.setBoolean(5, false); // Default 2FA disabled
                
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    } else {
                        return false;
                    }
                }
            }
            
            // Assign role
            if (userId != -1) {
                String targetRole = roleName != null ? roleName : "CANDIDATE";
                Role role = roleDAO.getRoleByName(targetRole);
                if (role != null) {
                    roleDAO.assignRoleToUser(userId, role.getId());
                }
            }
            
            return true;
        }
    }

    /**
     * Validates username/email and password. Supports BCrypt (new) and SHA-256 (legacy) hashes.
     */
    public boolean validateCredentials(String identifier, String rawPassword) throws SQLException {
        String sql = "SELECT password_hash, is_active FROM users WHERE username = ? OR email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try {
                        boolean isActive = rs.getBoolean("is_active");
                        if (!isActive) return false; // Account disabled
                    } catch (SQLException e) {
                        // Ignore if column missing
                    }

                    String stored = rs.getString("password_hash");
                    if (stored == null) return false;
                    // If it's a BCrypt hash
                    if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                        return BCrypt.checkpw(rawPassword, stored);
                    }
                    // Legacy SHA-256 fallback
                    String legacy = sha256(rawPassword);
                    return stored.equals(legacy);
                }
            }
        }
        return false;
    }

    /**
     * Fetches a user profile (without password hash) by username or email.
     */
    public User getByUsername(String identifier) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            String sql = "SELECT id, username, email, is_active, two_factor_enabled FROM users WHERE username = ? OR email = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, identifier);
                ps.setString(2, identifier);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setEmail(rs.getString("email"));
                        try {
                            u.setActive(rs.getBoolean("is_active"));
                            u.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled"));
                        } catch (SQLException e) {
                            u.setActive(true);
                        }
                        
                        u.setRoles(roleDAO.getRolesForUser(u.getId()));
                        return u;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Fetch last used course & chapter IDs for a user. Returns Integer[]{courseId, chapterId} where values may be null.
     */
    public Integer[] getLastUsedIds(int userId) throws SQLException {
        String sql = "SELECT last_course_id, last_chapter_id FROM users WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer courseId = rs.getObject("last_course_id") != null ? rs.getInt("last_course_id") : null;
                    Integer chapterId = rs.getObject("last_chapter_id") != null ? rs.getInt("last_chapter_id") : null;
                    return new Integer[]{ courseId, chapterId };
                }
            }
        }
        return new Integer[]{ null, null };
    }

    /**
     * Update last used course/chapter for a user. Pass null to clear a value.
     */
    public boolean updateLastUsed(int userId, Integer lastCourseId, Integer lastChapterId) throws SQLException {
        String sql = "UPDATE users SET last_course_id = ?, last_chapter_id = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (lastCourseId != null) ps.setInt(1, lastCourseId); else ps.setNull(1, java.sql.Types.INTEGER);
            if (lastChapterId != null) ps.setInt(2, lastChapterId); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setInt(3, userId);
            return ps.executeUpdate() == 1;
        }
    }

    private static String sha256(String text) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    /**
     * Get top learners by total XP (for leaderboard display)
     */
    public List<Map<String, Object>> getTopLearners(int limit) throws SQLException {
        List<Map<String, Object>> learners = new ArrayList<>();
        String query = "SELECT u.id, u.username, COALESCE(SUM(p.xp), 0) as total_xp " +
                       "FROM users u " +
                       "LEFT JOIN progress p ON u.id = p.user_id " +
                       "GROUP BY u.id, u.username " +
                       "ORDER BY total_xp DESC " +
                       "LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    Map<String, Object> learner = new HashMap<>();
                    learner.put("rank", rank++);
                    learner.put("username", rs.getString("username"));
                    learner.put("total_xp", rs.getInt("total_xp"));
                    learner.put("user_id", rs.getInt("id"));
                    learners.add(learner);
                }
            }
        }
        return learners;
    }

    /**
     * Update user's email address
     */
    public boolean updateEmail(int userId, String newEmail) throws SQLException {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newEmail);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() == 1;
        }
    }

    /**
     * Get user by ID
     */
    public User getById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            String sql = "SELECT id, username, email, is_active, two_factor_enabled FROM users WHERE id = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setEmail(rs.getString("email"));
                        try {
                            u.setActive(rs.getBoolean("is_active"));
                            u.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled"));
                        } catch (SQLException e) {
                            u.setActive(true);
                        }
                        
                        u.setRoles(roleDAO.getRolesForUser(u.getId()));
                        return u;
                    }
                }
            }
        }
        return null;
    }

    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, is_active = ?, two_factor_enabled = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setBoolean(2, user.isActive());
            ps.setBoolean(3, user.isTwoFactorEnabled());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        }
    }
    
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public List<User> getAllAdmins() throws SQLException {
        List<User> admins = new ArrayList<>();
        String sql = "SELECT DISTINCT u.id, u.username, u.email, u.is_active, u.two_factor_enabled " +
                     "FROM users u " +
                     "JOIN user_roles ur ON u.id = ur.user_id " +
                     "JOIN roles r ON ur.role_id = r.id " +
                     "WHERE r.name != 'CANDIDATE'";
                     
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                try {
                    u.setActive(rs.getBoolean("is_active"));
                    u.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled"));
                } catch (SQLException e) {
                    u.setActive(true);
                }
                u.setRoles(roleDAO.getRolesForUser(u.getId()));
                admins.add(u);
            }
        }
        return admins;
    }

    /**
     * Get all users (for admin panel)
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email, is_active, two_factor_enabled FROM users";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                try {
                    u.setActive(rs.getBoolean("is_active"));
                    u.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled"));
                } catch (SQLException e) {
                    u.setActive(true);
                }
                u.setRoles(roleDAO.getRolesForUser(u.getId()));
                users.add(u);
            }
        }
        return users;
    }
}
