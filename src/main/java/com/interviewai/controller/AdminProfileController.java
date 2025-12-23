package com.interviewai.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.interviewai.dao.UserDAO;
import com.interviewai.model.User;
import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;
import com.interviewai.util.SessionContext;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Admin Profile page.
 * Allows admin users to view and edit their profile information.
 */
public class AdminProfileController implements Initializable {

    // Sidebar controller
    @FXML private AdminSidebarController adminSidebarController;

    // Header stats
    @FXML private Label roleLabel;
    @FXML private Label lastLoginLabel;
    @FXML private Label statusLabel;

    // Banner
    @FXML private Label bannerNameLabel;
    @FXML private Label bannerEmailLabel;

    // Account fields
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private Button editNameBtn;
    @FXML private Button saveNameBtn;
    @FXML private Button cancelNameBtn;

    // Password fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Stats labels
    @FXML private Label createdAtLabel;
    @FXML private Label lastPasswordChangeLabel;
    @FXML private Label usersManagedLabel;
    @FXML private Label coursesCountLabel;
    @FXML private Label sessionStartLabel;
    @FXML private Label ipAddressLabel;

    // DAO
    private final UserDAO userDAO = new UserDAO();
    private User currentUser;
    private String originalName;

    // Date formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set sidebar active page
        if (adminSidebarController != null) {
            adminSidebarController.setActivePage("profile");
        }

        // Load current user data
        loadUserData();

        // Load stats
        loadStats();
    }

    /**
     * Load the current user's data.
     */
    private void loadUserData() {
        currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        // Update header stats
        if (roleLabel != null) {
            roleLabel.setText(formatRole(currentUser.getRole()));
        }
        if (lastLoginLabel != null) {
            lastLoginLabel.setText("Today");
        }
        if (statusLabel != null) {
            statusLabel.setText(currentUser.isActive() ? "Active" : "Inactive");
        }

        // Update banner
        if (bannerNameLabel != null) {
            bannerNameLabel.setText(currentUser.getUsername());
        }
        if (bannerEmailLabel != null) {
            bannerEmailLabel.setText(currentUser.getEmail());
        }

        // Update account fields
        if (nameField != null) {
            nameField.setText(currentUser.getUsername());
            originalName = nameField.getText();
        }
        if (emailField != null) {
            emailField.setText(currentUser.getEmail());
        }
        if (usernameField != null) {
            usernameField.setText(currentUser.getUsername());
        }

        // Update account stats
        if (createdAtLabel != null) {
            createdAtLabel.setText("—"); // Not available in User model
        }
        if (lastPasswordChangeLabel != null) {
            lastPasswordChangeLabel.setText("—"); // Not tracked in current schema
        }
        if (sessionStartLabel != null) {
            sessionStartLabel.setText(LocalDateTime.now().format(timeFormatter));
        }
    }

    /**
     * Load statistics.
     */
    private void loadStats() {
        try {
            // Count total users (excluding super admin)
            int totalUsers = countUsers();
            if (usersManagedLabel != null) {
                usersManagedLabel.setText(String.valueOf(totalUsers));
            }

            // Count total courses
            int totalCourses = countCourses();
            if (coursesCountLabel != null) {
                coursesCountLabel.setText(String.valueOf(totalCourses));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Count total users.
     */
    private int countUsers() {
        try {
            java.sql.Connection conn = com.interviewai.dao.DBConnection.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role != 'SUPER_ADMIN'");
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                ps.close();
                conn.close();
                return count;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Count total courses.
     */
    private int countCourses() {
        try {
            java.sql.Connection conn = com.interviewai.dao.DBConnection.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM generated_courses");
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                ps.close();
                conn.close();
                return count;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Format role for display.
     */
    private String formatRole(String role) {
        if (role == null) return "Admin";
        return switch (role) {
            case "SUPER_ADMIN" -> "Super Admin";
            case "CONTENT_ADMIN" -> "Content Admin";
            case "AI_MANAGER" -> "AI Manager";
            case "MODERATOR" -> "Moderator";
            case "ANALYST" -> "Analyst";
            default -> role;
        };
    }

    /**
     * Edit profile button handler.
     */
    @FXML
    private void onEditProfile() {
        onEditName();
    }

    /**
     * Edit name button handler.
     */
    @FXML
    private void onEditName() {
        nameField.setDisable(false);
        nameField.requestFocus();
        editNameBtn.setVisible(false);
        editNameBtn.setManaged(false);
        saveNameBtn.setVisible(true);
        saveNameBtn.setManaged(true);
        cancelNameBtn.setVisible(true);
        cancelNameBtn.setManaged(true);
    }

    /**
     * Save name button handler.
     */
    @FXML
    private void onSaveName() {
        String newName = nameField.getText().trim();
        if (newName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name cannot be empty.");
            return;
        }

        try {
            // Update user in database
            currentUser.setUsername(newName);
            userDAO.updateUser(currentUser);

            // Update session
            SessionContext.setCurrentUser(currentUser);

            // Update UI
            originalName = newName;
            bannerNameLabel.setText(newName);
            onCancelEditName();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Name updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update name: " + e.getMessage());
        }
    }

    /**
     * Cancel edit name button handler.
     */
    @FXML
    private void onCancelEditName() {
        nameField.setText(originalName);
        nameField.setDisable(true);
        editNameBtn.setVisible(true);
        editNameBtn.setManaged(true);
        saveNameBtn.setVisible(false);
        saveNameBtn.setManaged(false);
        cancelNameBtn.setVisible(false);
        cancelNameBtn.setManaged(false);
    }

    /**
     * Change password button handler.
     */
    @FXML
    private void onChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (currentPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter your current password.");
            return;
        }

        if (newPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a new password.");
            return;
        }

        if (newPassword.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Error", "New password must be at least 6 characters.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match.");
            return;
        }

        try {
            // Verify current password
            boolean isValid = userDAO.validateCredentials(currentUser.getUsername(), currentPassword);
            if (!isValid) {
                showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect.");
                return;
            }

            // Update password
            userDAO.updatePassword(currentUser.getId(), newPassword);

            // Clear fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to change password: " + e.getMessage());
        }
    }

    /**
     * Logout button handler.
     */
    @FXML
    private void onLogout() {
        SessionContext.clear();
        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            SceneNavigator.switchTo(stage, Routes.LOGIN, stage.getWidth(), stage.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
