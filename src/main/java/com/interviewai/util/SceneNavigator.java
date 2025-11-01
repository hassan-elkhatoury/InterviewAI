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
        Parent root = FXMLLoader.load(SceneNavigator.class.getResource(fxmlPath));
        stage.setScene(new Scene(root, width, height));
        stage.show();
    }
}
