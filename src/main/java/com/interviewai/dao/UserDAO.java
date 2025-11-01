package com.interviewai.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.interviewai.model.User;

/**
 * Basic JDBC-backed DAO for users.
 */
public class UserDAO {

    /**
     * Creates a user if username is not taken. Password is hashed with BCrypt.
     * Returns true if a row was inserted.
     */
    public boolean createUser(String username, String email, String rawPassword, String role) throws SQLException {
        String sqlCheck = "SELECT id FROM users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement psCheck = c.prepareStatement(sqlCheck)) {
            psCheck.setString(1, username);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) return false; // username taken
            }
        }
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        
        // Try with role column first; fallback to without role if column doesn't exist
        try (Connection c = DBConnection.getConnection()) {
            String sqlInsert = "INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sqlInsert)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, hash);
                ps.setString(4, role);
                return ps.executeUpdate() == 1;
            } catch (SQLException e) {
                // If role column doesn't exist, try without it
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown column")) {
                    String sqlInsertNoRole = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
                    try (PreparedStatement ps2 = c.prepareStatement(sqlInsertNoRole)) {
                        ps2.setString(1, username);
                        ps2.setString(2, email);
                        ps2.setString(3, hash);
                        return ps2.executeUpdate() == 1;
                    }
                }
                throw e;
            }
        }
    }

    /**
     * Validates username/password. Supports BCrypt (new) and SHA-256 (legacy) hashes.
     */
    public boolean validateCredentials(String username, String rawPassword) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString(1);
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
     * Fetches a user profile (without password hash) by username.
     */
    public User getByUsername(String username) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            // Try with role column first
            String sql = "SELECT id, username, email, role FROM users WHERE username = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setUsername(rs.getString("username"));
                        u.setEmail(rs.getString("email"));
                        String role = rs.getString("role");
                        u.setRole(role != null ? role : "CANDIDATE");
                        return u;
                    }
                }
            } catch (SQLException e) {
                // If role column doesn't exist, try without it
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unknown column")) {
                    String sqlNoRole = "SELECT id, username, email FROM users WHERE username = ?";
                    try (PreparedStatement ps2 = c.prepareStatement(sqlNoRole)) {
                        ps2.setString(1, username);
                        try (ResultSet rs = ps2.executeQuery()) {
                            if (rs.next()) {
                                User u = new User();
                                u.setId(rs.getInt("id"));
                                u.setUsername(rs.getString("username"));
                                u.setEmail(rs.getString("email"));
                                u.setRole("CANDIDATE"); // default role
                                return u;
                            }
                        }
                    }
                } else {
                    throw e;
                }
            }
        }
        return null;
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
}
