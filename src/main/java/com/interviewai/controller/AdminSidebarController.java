package com.interviewai.controller;

import java.io.IOException;

import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;
import com.interviewai.util.SessionContext;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controller for the shared Admin Sidebar component.
 * This sidebar is reused across all admin pages.
 */
public class AdminSidebarController {

    // Navigation Buttons
    @FXML private Button dashboardBtn;
    @FXML private Button userManagementBtn;
    @FXML private Button coursesBtn;
    @FXML private Button profileBtn;

    // Icon containers for active state styling
    @FXML private StackPane dashboardIconContainer;
    @FXML private StackPane userManagementIconContainer;
    @FXML private StackPane coursesIconContainer;
    @FXML private StackPane profileIconContainer;

    // Labels for active state styling
    @FXML private Label dashboardLabel;
    @FXML private Label userManagementLabel;
    @FXML private Label coursesLabel;
    @FXML private Label profileLabel;

    // Dashboard badge
    @FXML private Label dashboardBadge;

    // Admin profile
    @FXML private Label adminNameLabel;

    private String currentPage = "";

    @FXML
    public void initialize() {
        // Set admin name from session if available
        if (SessionContext.getCurrentUser() != null) {
            String username = SessionContext.getCurrentUser().getUsername();
            if (adminNameLabel != null && username != null) {
                adminNameLabel.setText(username);
            }
        }
    }

    /**
     * Set the current active page to highlight in the sidebar.
     * @param page The current page identifier (e.g., "dashboard", "users", "courses", "analytics")
     */
    public void setActivePage(String page) {
        this.currentPage = page;
        updateActiveState();
    }

    private void updateActiveState() {
        // Reset all buttons to inactive state
        resetButtonStates();

        // Set the active button based on current page
        switch (currentPage.toLowerCase()) {
            case "dashboard":
                setButtonActive(dashboardBtn, dashboardIconContainer, dashboardLabel);
                if (dashboardBadge != null) {
                    dashboardBadge.setVisible(true);
                    dashboardBadge.setManaged(true);
                }
                break;
            case "users":
            case "user-management":
                setButtonActive(userManagementBtn, userManagementIconContainer, userManagementLabel);
                break;
            case "courses":
                setButtonActive(coursesBtn, coursesIconContainer, coursesLabel);
                break;
            case "profile":
                setButtonActive(profileBtn, profileIconContainer, profileLabel);
                break;
        }
    }

    private void resetButtonStates() {
        // Reset dashboard
        if (dashboardBtn != null) {
            dashboardBtn.getStyleClass().removeAll("nav-active");
        }
        if (dashboardIconContainer != null) {
            dashboardIconContainer.getStyleClass().removeAll("nav-icon-container-active");
            if (!dashboardIconContainer.getStyleClass().contains("nav-icon-container")) {
                dashboardIconContainer.getStyleClass().add("nav-icon-container");
            }
        }
        if (dashboardLabel != null) {
            dashboardLabel.getStyleClass().removeAll("nav-label-active");
            if (!dashboardLabel.getStyleClass().contains("nav-label")) {
                dashboardLabel.getStyleClass().add("nav-label");
            }
        }
        if (dashboardBadge != null) {
            dashboardBadge.setVisible(false);
            dashboardBadge.setManaged(false);
        }

        // Reset user management
        if (userManagementBtn != null) {
            userManagementBtn.getStyleClass().removeAll("nav-active");
        }
        if (userManagementIconContainer != null) {
            userManagementIconContainer.getStyleClass().removeAll("nav-icon-container-active");
            if (!userManagementIconContainer.getStyleClass().contains("nav-icon-container")) {
                userManagementIconContainer.getStyleClass().add("nav-icon-container");
            }
        }
        if (userManagementLabel != null) {
            userManagementLabel.getStyleClass().removeAll("nav-label-active");
            if (!userManagementLabel.getStyleClass().contains("nav-label")) {
                userManagementLabel.getStyleClass().add("nav-label");
            }
        }

        // Reset courses
        if (coursesBtn != null) {
            coursesBtn.getStyleClass().removeAll("nav-active");
        }
        if (coursesIconContainer != null) {
            coursesIconContainer.getStyleClass().removeAll("nav-icon-container-active");
            if (!coursesIconContainer.getStyleClass().contains("nav-icon-container")) {
                coursesIconContainer.getStyleClass().add("nav-icon-container");
            }
        }
        if (coursesLabel != null) {
            coursesLabel.getStyleClass().removeAll("nav-label-active");
            if (!coursesLabel.getStyleClass().contains("nav-label")) {
                coursesLabel.getStyleClass().add("nav-label");
            }
        }

        // Reset profile
        if (profileBtn != null) {
            profileBtn.getStyleClass().removeAll("nav-active");
        }
        if (profileIconContainer != null) {
            profileIconContainer.getStyleClass().removeAll("nav-icon-container-active");
            if (!profileIconContainer.getStyleClass().contains("nav-icon-container")) {
                profileIconContainer.getStyleClass().add("nav-icon-container");
            }
        }
        if (profileLabel != null) {
            profileLabel.getStyleClass().removeAll("nav-label-active");
            if (!profileLabel.getStyleClass().contains("nav-label")) {
                profileLabel.getStyleClass().add("nav-label");
            }
        }
    }

    private void setButtonActive(Button button, StackPane iconContainer, Label label) {
        if (button != null && !button.getStyleClass().contains("nav-active")) {
            button.getStyleClass().add("nav-active");
        }
        if (iconContainer != null) {
            iconContainer.getStyleClass().removeAll("nav-icon-container");
            if (!iconContainer.getStyleClass().contains("nav-icon-container-active")) {
                iconContainer.getStyleClass().add("nav-icon-container-active");
            }
        }
        if (label != null) {
            label.getStyleClass().removeAll("nav-label");
            if (!label.getStyleClass().contains("nav-label-active")) {
                label.getStyleClass().add("nav-label-active");
            }
        }
    }

    // =============== Navigation Methods ===============

    @FXML
    public void onNavigateDashboard(ActionEvent event) {
        if (!"dashboard".equals(currentPage)) {
            navigateTo(Routes.ADMIN);
        }
    }

    @FXML
    public void onNavigateUserManagement(ActionEvent event) {
        if (!"users".equals(currentPage) && !"user-management".equals(currentPage)) {
            navigateTo(Routes.USER_MANAGEMENT);
        }
    }

    @FXML
    public void onNavigateCourses(ActionEvent event) {
        if (!"courses".equals(currentPage)) {
            navigateTo(Routes.COURSES_MANAGEMENT);
        }
    }

    @FXML
    public void onNavigateProfile(ActionEvent event) {
        if (!"profile".equals(currentPage)) {
            navigateTo(Routes.ADMIN_PROFILE);
        }
    }

    @FXML
    public void onLogout(ActionEvent event) {
        SessionContext.clear();
        navigateTo(Routes.LOGIN);
    }

    private void navigateTo(String route) {
        try {
            Stage stage = getStage();
            if (stage != null) {
                SceneNavigator.switchTo(stage, route, stage.getWidth(), stage.getHeight());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Navigation failed: " + e.getMessage());
        }
    }

    private Stage getStage() {
        if (dashboardBtn != null && dashboardBtn.getScene() != null) {
            return (Stage) dashboardBtn.getScene().getWindow();
        }
        return null;
    }
}
