package com.interviewai.controller;

import com.interviewai.model.User;
import com.interviewai.service.AuthService;
import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;
import com.interviewai.util.SessionContext;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();
        boolean ok = authService.authenticate(user, pass);
        if (ok) {
            User u = authService.getDemoUser(user);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Login Successful");
            a.setHeaderText(null);
            a.setContentText("Welcome, " + (u != null ? u.getUsername() : user) + "!");
            a.showAndWait();
            // TODO: Replace demo routing with real onboarding state check.
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                SessionContext.setCurrentUser(u);
                if (u != null && "ADMIN".equalsIgnoreCase(u.getRole())) {
                    // Admin route
                    SceneNavigator.switchTo(stage, Routes.ADMIN, 900, 600);
                } else {
                    // Candidate route -> Onboarding first, later can go to Dashboard
                    SceneNavigator.switchTo(stage, Routes.ONBOARDING, 900, 600);
                }
            } catch (Exception ex) {
                // TODO: centralize error dialogs
                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to open next screen: " + ex.getMessage());
                err.showAndWait();
            }
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Login Failed");
            a.setHeaderText(null);
            a.setContentText("Invalid username or password. For demo use admin/password.");
            a.showAndWait();
        }
    }
    
    @FXML
    private void onOpenCreateAccount(ActionEvent event) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            SceneNavigator.switchTo(stage, Routes.CREATE_ACCOUNT, 900, 600);
        } catch (Exception ex) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to open Create Account: " + ex.getMessage());
            err.showAndWait();
        }
    }
}
