package com.interviewai.controller;

import com.interviewai.model.User;
import com.interviewai.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    private AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
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
            // TODO: switch scene to main app view
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Login Failed");
            a.setHeaderText(null);
            a.setContentText("Invalid username or password. For demo use admin/password.");
            a.showAndWait();
        }
    }
}
