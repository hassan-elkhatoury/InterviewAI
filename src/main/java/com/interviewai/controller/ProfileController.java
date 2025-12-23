package com.interviewai.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.mindrot.jbcrypt.BCrypt;

import com.interviewai.dao.ProgressDAO;
import com.interviewai.dao.UserDAO;
import com.interviewai.model.User;
import com.interviewai.util.SessionContext;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

/**
 * Controller for the user profile screen.
 * Allows users to view and edit their account information, preferences, and security settings.
 */
public class ProfileController implements Initializable {

    // Banner Components
    @FXML private Label bannerNameLabel;
    @FXML private Label bannerXPLabel;
    @FXML private Label bannerLevelLabel;
    @FXML private Label bannerStreakLabel;
    @FXML private Label bannerCoursesLabel;

    // Account Information Fields
    @FXML private TextField nameField;

    // Security Fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Name Edit Buttons
    @FXML private Button editNameButton;
    @FXML private Button saveNameButton;
    @FXML private Button cancelNameButton;

    // Password Save Button
    @FXML private Button savePasswordButton;

    // Delete Account Button
    @FXML private Button deleteAccountButton;

    // Models and DAOs
    private User currentUser;
    private UserDAO userDAO;
    private ProgressDAO progressDAO;
    
    // Track original name for cancel
    private String originalName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        progressDAO = new ProgressDAO();
        currentUser = SessionContext.getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user logged in", "Please log in to view your profile.");
            return;
        }

        try {
            loadUserProfile();
            loadUserStats();
            setupEventHandlers();
            activateProfileSidebarButton();
        } catch (SQLException e) {
            System.err.println("Error loading profile: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load profile", e.getMessage());
        }
    }

    /**
     * Activate the profile button in sidebar
     */
    private void activateProfileSidebarButton() {
        javafx.application.Platform.runLater(() -> {
            MainLayoutController mainLayout = MainLayoutController.getInstance();
            if (mainLayout != null && mainLayout.getSidebarController() != null) {
                mainLayout.getSidebarController().setActiveButton("profile");
            }
        });
    }

    /**
     * Load user profile information from database and display it
     */
    private void loadUserProfile() throws SQLException {
        // Refresh user data from database to get latest info
        User latestUser = userDAO.getByUsername(currentUser.getUsername());
        if (latestUser != null) {
            currentUser = latestUser;
            SessionContext.setCurrentUser(currentUser);
        }

        // Display user information
        bannerNameLabel.setText("Welcome, " + currentUser.getUsername() + "");
        nameField.setText(currentUser.getUsername());
        originalName = currentUser.getUsername();
    }

    /**
     * Load user statistics from database
     */
    private void loadUserStats() throws SQLException {
        // Get total XP
        int totalXP = progressDAO.getTotalXPForUser(currentUser.getId());
        String formattedXP = formatNumber(totalXP);
        bannerXPLabel.setText(formattedXP);

        // Get user level based on XP (every 1000 XP = 1 level)
        int level = Math.max(1, totalXP / 1000 + 1);
        bannerLevelLabel.setText(String.valueOf(level));

        // Streak (real calculation)
        int streak = progressDAO.calculateUserStreak(currentUser.getId());
        bannerStreakLabel.setText(String.valueOf(streak));

        // Courses count (placeholder)
        bannerCoursesLabel.setText("3");
    }

    /**
     * Format numbers with thousands separator
     */
    private String formatNumber(int num) {
        return String.format("%,d", num);
    }

    /**
     * Setup event handlers for buttons
     */
    private void setupEventHandlers() {
        // Name edit buttons
        editNameButton.setOnAction(e -> onEditName());
        saveNameButton.setOnAction(e -> onSaveName());
        cancelNameButton.setOnAction(e -> onCancelName());
        
        // Password save button
        savePasswordButton.setOnAction(e -> onSavePassword());
        
        // Delete account button
        deleteAccountButton.setOnAction(e -> onDeleteAccount());
    }

    /**
     * Enable name field for editing
     */
    @FXML
    private void onEditName() {
        nameField.setDisable(false);
        nameField.requestFocus();
        nameField.selectAll();
        
        // Hide edit button, show save/cancel buttons
        editNameButton.setVisible(false);
        editNameButton.setManaged(false);
        saveNameButton.setVisible(true);
        saveNameButton.setManaged(true);
        cancelNameButton.setVisible(true);
        cancelNameButton.setManaged(true);
    }

    /**
     * Save the new display name
     */
    @FXML
    private void onSaveName() {
        String newName = nameField.getText().trim();
        
        if (newName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name Required", "Please enter a display name.");
            return;
        }
        
        if (newName.length() < 2) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Name", "Display name must be at least 2 characters long.");
            return;
        }
        
        try {
            // Persist to DB
            String previousName = currentUser.getUsername();
            currentUser.setUsername(newName);
            userDAO.updateUser(currentUser);
            
            // Update session and UI only if DB update succeeds
            SessionContext.setCurrentUser(currentUser);
            bannerNameLabel.setText("Welcome, " + newName + "");
            originalName = newName;
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Name Updated", "Your display name has been updated!");
            
            // Disable name field and hide save/cancel buttons
            nameField.setDisable(true);
            editNameButton.setVisible(true);
            editNameButton.setManaged(true);
            saveNameButton.setVisible(false);
            saveNameButton.setManaged(false);
            cancelNameButton.setVisible(false);
            cancelNameButton.setManaged(false);
            
        } catch (SQLException e) {
            // Revert local change on error
            currentUser.setUsername(originalName);
            System.err.println("Error updating name: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to Update Name", e.getMessage());
        }
    }

    /**
     * Cancel name editing
     */
    @FXML
    private void onCancelName() {
        nameField.setText(originalName);
        nameField.setDisable(true);
        
        // Hide save/cancel buttons, show edit button
        editNameButton.setVisible(true);
        editNameButton.setManaged(true);
        saveNameButton.setVisible(false);
        saveNameButton.setManaged(false);
        cancelNameButton.setVisible(false);
        cancelNameButton.setManaged(false);
    }

    /**
     * Save password changes
     */
    @FXML
    private void onSavePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() && confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "No Password Entered", 
                "Please enter a new password.");
            return;
        }

        if (currentPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Current Password Required", 
                "Please enter your current password to change it.");
            return;
        }

        try {
            if (!userDAO.validateCredentials(currentUser.getUsername(), currentPassword)) {
                showAlert(Alert.AlertType.ERROR, "Authentication Error", "Incorrect Password", 
                    "The current password you entered is incorrect.");
                return;
            }

            if (newPassword.length() < 8) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Password Too Short", 
                    "Password must be at least 8 characters long.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Passwords Don't Match", 
                    "New password and confirmation don't match.");
                return;
            }

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
            boolean passwordUpdated = userDAO.updatePassword(currentUser.getId(), hashedPassword);

            if (passwordUpdated) {
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Password Updated", 
                    "Your password has been successfully updated!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to Update Password", 
                    "An error occurred while updating your password. Please try again.");
            }

        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to Update Password", e.getMessage());
        }
    }

    /**
     * Handle Delete Account button click
     */
    @FXML
    private void onDeleteAccount() {
        Alert confirmDialog = new Alert(Alert.AlertType.WARNING);
        confirmDialog.setTitle("Delete Account");
        confirmDialog.setHeaderText("Are you sure?");
        confirmDialog.setContentText("Deleting your account will permanently remove all your data and cannot be undone.");
        
        confirmDialog.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        
        var result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Show password confirmation dialog
            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Confirm Account Deletion");
            passwordDialog.setHeaderText("Enter your password to confirm:");
            passwordDialog.setContentText("Password:");
            
            var passwordResult = passwordDialog.showAndWait();
            
            if (passwordResult.isPresent()) {
                String enteredPassword = passwordResult.get();
                
                if (enteredPassword.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Password Required", 
                        "Please enter your password to confirm account deletion.");
                    return;
                }
                
                try {
                    if (!userDAO.validateCredentials(currentUser.getUsername(), enteredPassword)) {
                        showAlert(Alert.AlertType.ERROR, "Authentication Error", "Incorrect Password", 
                            "The password you entered is incorrect.");
                        return;
                    }
                    
                    showAlert(Alert.AlertType.INFORMATION, "Account Deletion Pending", "Success", 
                        "Your account deletion request has been received. Please contact support to complete the process.");
                    
                } catch (SQLException e) {
                    System.err.println("Error validating password: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to Delete Account", 
                        e.getMessage());
                }
            }
        }
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Refresh profile data
     */
    public void refreshProfile() {
        try {
            loadUserProfile();
            loadUserStats();
        } catch (SQLException e) {
            System.err.println("Error refreshing profile: " + e.getMessage());
        }
    }

    /**
     * Focus the password section from banner quick action
     */
    @FXML
    private void onFocusPasswordSection() {
        currentPasswordField.requestFocus();
    }
}
