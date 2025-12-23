package com.interviewai.main;

import com.interviewai.dao.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ResetPassword {
    public static void main(String[] args) {
        // Hash for "fahdfahd123" (taken from logs to ensure compatibility)
        String hash = "$2a$10$N3RL1A2iKJYl3mvr..nVA.Vby5d31rN7om6z2s5RaxFvmQh6IhNpy";
        String username = "fahddd";
        
        System.out.println("Resetting password for user: " + username);
        
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, hash);
            ps.setString(2, username);
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("SUCCESS: Password updated for " + username);
            } else {
                System.out.println("FAILURE: User " + username + " not found!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
