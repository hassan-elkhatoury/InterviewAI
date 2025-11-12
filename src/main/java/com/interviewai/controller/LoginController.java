package com.interviewai.controller;

import com.interviewai.dao.CourseDAO;
import com.interviewai.dao.OnboardingDAO;
import com.interviewai.dao.UserDAO;
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
        String userName = usernameField.getText();
        String pass = passwordField.getText();
        boolean ok = authService.authenticate(userName, pass);
        if (ok) {
            User u = authService.getUser(userName);
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();

                SessionContext.setCurrentUser(u);

                // Load last-used course/chapter into session so Dashboard opens the right one
                try {
                    UserDAO userDAO = new UserDAO();
                    Integer[] lastUsed = userDAO.getLastUsedIds(u.getId());
                    if (lastUsed != null) {
                        SessionContext.setActiveCourseId(lastUsed[0]);
                        SessionContext.setActiveChapterId(lastUsed[1]);
                    }
                } catch (Exception e) {
                    System.err.println("Could not load last-used course: " + e.getMessage());
                }
                
                if (u != null && "ADMIN".equalsIgnoreCase(u.getRole())) {

                    // Admin route
                    SceneNavigator.switchTo(stage, Routes.ADMIN, stage.getWidth()-15, stage.getHeight()-38);
                    
                } else {

                    CourseDAO courseDAO = new CourseDAO();
                    if(courseDAO.checkUserCourse(u.getId())){

                        SceneNavigator.switchTo(stage, Routes.DASHBOARD, stage.getWidth()-15, stage.getHeight()-38);

                    } else{

                        SceneNavigator.switchTo(stage, Routes.ONBOARDING, stage.getWidth()-15, stage.getHeight()-38);

                    }

                    
                }
            } catch (Exception ex) {
                System.err.println("=== ERROR IN LOGIN FLOW ===");
                System.err.println("Exception type: " + ex.getClass().getName());
                System.err.println("Message: " + ex.getMessage());
                ex.printStackTrace();
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
            SceneNavigator.switchTo(stage, Routes.CREATE_ACCOUNT, stage.getWidth()-15, stage.getHeight()-38);
        } catch (Exception ex) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to open Create Account: " + ex.getMessage());
            err.showAndWait();
        }
    }

    @FXML
    private void onForgotPassword(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Password Help");
        a.setHeaderText("Forgot password");
        a.setContentText("Password reset isn't set up yet. Please contact support or try a demo user: admin/password.");
        a.showAndWait();
    }
}
