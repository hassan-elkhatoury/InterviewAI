package com.interviewai.util;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Simple navigation helper for switching scenes.
 * Usage: SceneNavigator.switchTo(stage, "/fxml/DashboardView.fxml", 900, 600);
 * TODO: Centralize route names and role-based guards.
 */
public class SceneNavigator {
    public static void switchTo(Stage stage, String fxmlPath, double width, double height) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    public static void switchTo(Stage stage, String fxmlPath) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to load FXML: " + fxmlPath, e);
        }
    }
}
