package com.interviewai.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
            primaryStage.setTitle("InterviewAI");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Startup Error");
            a.setHeaderText("Failed to load initial view");
            a.setContentText(String.valueOf(e.getMessage()));
            a.showAndWait();
        }
    }

    public static void main(String[] args) {
        // TEMP FIX: Ensure schema has daily_quest_last_reset
        try {
             try (java.sql.Connection c = com.interviewai.dao.DBConnection.getConnection()) {
                boolean colExists = false;
                try (java.sql.Statement s = c.createStatement()) {
                    s.executeQuery("SELECT daily_quest_last_reset FROM users LIMIT 1");
                    colExists = true;
                } catch (java.sql.SQLException e) {}
                
                if (!colExists) {
                    System.out.println("--- ADDING MISSING COLUMN daily_quest_last_reset TO users ---");
                    try (java.sql.Statement s = c.createStatement()) {
                        // Initialize to start of today so existing users start fresh
                        s.executeUpdate("ALTER TABLE users ADD COLUMN daily_quest_last_reset DATETIME DEFAULT (DATE_SUB(NOW(), INTERVAL 1 DAY))");
                    }
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // TEMP FIX: Ensure schema has daily_quest_claims_count
        try {
             try (java.sql.Connection c = com.interviewai.dao.DBConnection.getConnection()) {
                boolean colExists = false;
                try (java.sql.Statement s = c.createStatement()) {
                    s.executeQuery("SELECT daily_quest_claims_count FROM users LIMIT 1");
                    colExists = true;
                } catch (java.sql.SQLException e) {}
                
                if (!colExists) {
                    System.out.println("--- ADDING MISSING COLUMN daily_quest_claims_count TO users ---");
                    try (java.sql.Statement s = c.createStatement()) {
                        s.executeUpdate("ALTER TABLE users ADD COLUMN daily_quest_claims_count INT DEFAULT 0");
                    }
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // TEMP FIX: Ensure schema has quest claim columns
        try {
             try (java.sql.Connection c = com.interviewai.dao.DBConnection.getConnection()) {
                // Check last_daily_quest_claim
                boolean dailyExists = false;
                try (java.sql.Statement s = c.createStatement()) {
                    s.executeQuery("SELECT last_daily_quest_claim FROM users LIMIT 1");
                    dailyExists = true;
                } catch (java.sql.SQLException e) {}
                
                if (!dailyExists) {
                    System.out.println("--- ADDING MISSING COLUMN last_daily_quest_claim TO users ---");
                    try (java.sql.Statement s = c.createStatement()) {
                        s.executeUpdate("ALTER TABLE users ADD COLUMN last_daily_quest_claim DATE NULL");
                    }
                }

                // Check last_monthly_quest_claim
                boolean monthlyExists = false;
                try (java.sql.Statement s = c.createStatement()) {
                    s.executeQuery("SELECT last_monthly_quest_claim FROM users LIMIT 1");
                    monthlyExists = true;
                } catch (java.sql.SQLException e) {}

                if (!monthlyExists) {
                    System.out.println("--- ADDING MISSING COLUMN last_monthly_quest_claim TO users ---");
                    try (java.sql.Statement s = c.createStatement()) {
                        s.executeUpdate("ALTER TABLE users ADD COLUMN last_monthly_quest_claim DATE NULL");
                    }
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // TEMP FIX: Ensure chapters has completed_at column
        try {
             try (java.sql.Connection c = com.interviewai.dao.DBConnection.getConnection()) {
                boolean colExists = false;
                try (java.sql.Statement s = c.createStatement()) {
                    s.executeQuery("SELECT completed_at FROM chapters LIMIT 1");
                    colExists = true;
                } catch (java.sql.SQLException e) {}
                
                if (!colExists) {
                    System.out.println("--- ADDING MISSING COLUMN completed_at TO chapters ---");
                    try (java.sql.Statement s = c.createStatement()) {
                        s.executeUpdate("ALTER TABLE chapters ADD COLUMN completed_at DATETIME NULL");
                    }
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // TEMP FIX: Recreate user_goals table to ensure correct schema
        try {
             try (java.sql.Connection c = com.interviewai.dao.DBConnection.getConnection()) {
                // Check if table exists
                boolean tableExists = false;
                try (java.sql.Statement s = c.createStatement()) {
                    s.executeQuery("SELECT 1 FROM user_goals LIMIT 1");
                    tableExists = true;
                } catch (java.sql.SQLException e) {}
                
                // If table exists, check if it has the correct columns. If not sure, DROP and RECREATE.
                // Since this is a new feature, safe to drop.
                
                boolean needsRecreate = false;
                if (tableExists) {
                    try (java.sql.Statement s = c.createStatement()) {
                        // Check for start_value column
                        s.executeQuery("SELECT start_value FROM user_goals LIMIT 1");
                    } catch (java.sql.SQLException e) {
                        System.out.println("--- DETECTED BROKEN SCHEMA: " + e.getMessage() + " ---");
                        needsRecreate = true;
                    }
                } else {
                    needsRecreate = true;
                }

                if (needsRecreate) {
                    System.out.println("--- RECREATING user_goals TABLE ---");
                    try (java.sql.Statement s = c.createStatement()) {
                        s.executeUpdate("DROP TABLE IF EXISTS user_goals");
                        s.executeUpdate(
                            "CREATE TABLE user_goals (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "user_id INT NOT NULL, " +
                            "goal_name VARCHAR(255) NOT NULL, " +
                            "goal_type VARCHAR(50) NOT NULL, " +
                            "target_value INT NOT NULL, " +
                            "start_value INT DEFAULT 0, " +
                            "current_value INT DEFAULT 0, " +
                            "is_active BOOLEAN DEFAULT 1, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                            "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                            ")"
                        );
                    }
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        launch(args);
    }
}
