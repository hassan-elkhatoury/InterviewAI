package com.interviewai.controller;

import com.interviewai.dao.UserDAO;
import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Handles user registration. Saves user to DB so they can log in.
 * TODO: Add stronger validation and password rules.
 */
public class CreateAccountController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void onCreate(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Username and password are required.").showAndWait();
            return;
        }
        try {
            boolean ok = userDAO.createUser(username, email, password, "CANDIDATE");
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Account created. You can now sign in.").showAndWait();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                SceneNavigator.switchTo(stage, Routes.LOGIN, 900, 600);
            } else {
                new Alert(Alert.AlertType.ERROR, "Username already exists or could not create account.").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to create account: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            SceneNavigator.switchTo(stage, Routes.LOGIN, 900, 600);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to go back: " + e.getMessage()).showAndWait();
        }
    }
}
