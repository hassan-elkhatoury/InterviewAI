package com.interviewai.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.interviewai.util.ConfigLoader;

/**
 * Centralized MySQL connection provider.
 * Loads credentials from src/main/resources/config/config.properties via ConfigLoader.
 */
public class DBConnection {

    public static Connection getConnection() throws SQLException {
        String url = ConfigLoader.get("db.url");
        String user = ConfigLoader.get("db.user");
        String pass = ConfigLoader.get("db.password");
        if (url == null || url.isEmpty()) {
            url = "jdbc:mysql://localhost:3306/interviewai"; // fallback
        }
        if (user == null) user = "root";
        if (pass == null) pass = "";
        return DriverManager.getConnection(url, user, pass);
    }
}
