package com.interviewai.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection conn;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            // Read connection from config.properties in production
            String url = "jdbc:mysql://localhost:3306/interviewai";
            String user = "root";
            String pass = "";
            conn = DriverManager.getConnection(url, user, pass);
        }
        return conn;
    }
}
