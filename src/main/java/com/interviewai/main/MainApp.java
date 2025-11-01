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
            primaryStage.setScene(new Scene(root, 900, 600));
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
        launch(args);
    }
}
