package com.interviewai.controller;

import com.interviewai.util.SessionContext;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

/**
 * Controller for the reusable sidebar component
 */
public class SidebarController {

    @FXML private Button dashboardButton;
    @FXML private Button lessonButton;
    @FXML private Button progressButton;
    @FXML private Button reviewButton;
    @FXML private Button questsButton;
    @FXML private Button leaderboardButton;
    @FXML private Button profileButton;
    @FXML private Button settingsButton;

    @FXML
    public void initialize() {
        // Check if a chapter is selected and show/hide lesson button
        updateLessonButtonVisibility();
    }

    /**
     * Update the visibility of the lesson button based on whether a chapter is selected
     */
    public void updateLessonButtonVisibility() {
        Integer chapterIdObj = SessionContext.getActiveChapterId();
        int chapterId = (chapterIdObj != null) ? chapterIdObj : 0;
        boolean hasChapter = chapterId > 0;
        
        if (lessonButton != null) {
            lessonButton.setVisible(hasChapter);
            lessonButton.setManaged(hasChapter);
            System.out.println("Lesson button visibility updated: " + hasChapter + " (Chapter ID: " + chapterId + ")");
        }
    }

    /**
     * Set the active navigation button
     */
    public void setActiveButton(String buttonName) {
        System.out.println("Setting active button to: " + buttonName);
        
        // Remove active class from all buttons
        dashboardButton.getStyleClass().remove("nav-active");
        if (lessonButton != null) {
            lessonButton.getStyleClass().remove("nav-active");
        }
        progressButton.getStyleClass().remove("nav-active");
        questsButton.getStyleClass().remove("nav-active");
        leaderboardButton.getStyleClass().remove("nav-active");
        profileButton.getStyleClass().remove("nav-active");
        settingsButton.getStyleClass().remove("nav-active");

        // Add active class to the specified button
        switch (buttonName.toLowerCase()) {
            case "dashboard":
                if (!dashboardButton.getStyleClass().contains("nav-active")) {
                    dashboardButton.getStyleClass().add("nav-active");
                    System.out.println("✓ Dashboard button activated");
                }
                break;
            case "lesson":
                if (lessonButton != null && !lessonButton.getStyleClass().contains("nav-active")) {
                    lessonButton.getStyleClass().add("nav-active");
                    System.out.println("✓ Lesson button activated");
                }
                break;
            case "progress":
                if (!progressButton.getStyleClass().contains("nav-active")) {
                    progressButton.getStyleClass().add("nav-active");
                    System.out.println("✓ Progress button activated");
                }
                break;
            case "review":
                if (!reviewButton.getStyleClass().contains("nav-active")) {
                    reviewButton.getStyleClass().add("nav-active");
                    System.out.println("✓ Review button activated");
                }
                break;
            case "quests":
                if (!questsButton.getStyleClass().contains("nav-active")) {
                    questsButton.getStyleClass().add("nav-active");
                    System.out.println("✓ Quests button activated");
                }
                break;
            case "leaderboard":
                if (!leaderboardButton.getStyleClass().contains("nav-active")) {
                    leaderboardButton.getStyleClass().add("nav-active");
                    System.out.println("✓ Leaderboard button activated");
                }
                break;
            case "profile":
                if (!profileButton.getStyleClass().contains("nav-active")) {
                    profileButton.getStyleClass().add("nav-active");
                    System.out.println("✓ Profile button activated");
                }
                break;
            case "settings":
                if (!settingsButton.getStyleClass().contains("nav-active")) {
                    settingsButton.getStyleClass().add("nav-active");
                    System.out.println("✓ Settings button activated");
                }
                break;
        }
    }

    @FXML
    private void onOpenDashboard() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadContent("/fxml/DashboardContent.fxml", "dashboard");
        }
    }

    @FXML
    private void onOpenLesson() {
        Integer chapterIdObj = SessionContext.getActiveChapterId();
        int chapterId = (chapterIdObj != null) ? chapterIdObj : 0;
        
        if (chapterId == 0) {
            // Show alert asking user to select a chapter
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Chapter Selected");
            alert.setHeaderText("Please Select a Chapter");
            alert.setContentText("You need to select a chapter from the dashboard before starting a lesson.\n\n" +
                               "Go to the Dashboard and click on a chapter to begin.");
            alert.showAndWait();
            
            // Navigate back to dashboard
            onOpenDashboard();
            return;
        }
        
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadContent("/fxml/LessonView.fxml", "lesson");
        }
    }

    @FXML
    private void onOpenProgress() {
        System.out.println("Progress view - Coming soon!");
        // MainLayoutController.getInstance().loadContent("/fxml/ProgressView.fxml", "progress");
    }

    @FXML
    private void onOpenReview() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadContent("/fxml/ReviewView.fxml", "review");
        }
    }

    @FXML
    private void onOpenQuests() {
        System.out.println("Quests view - Coming soon!");
        // MainLayoutController.getInstance().loadContent("/fxml/QuestsView.fxml", "quests");
    }

    @FXML
    private void onOpenLeaderboards() {
        System.out.println("Leaderboards view - Coming soon!");
        // MainLayoutController.getInstance().loadContent("/fxml/LeaderboardView.fxml", "leaderboard");
    }

    @FXML
    private void onOpenProfile() {
        System.out.println("Profile view - Coming soon!");
        // MainLayoutController.getInstance().loadContent("/fxml/ProfileView.fxml", "profile");
    }

    @FXML
    private void onOpenSettings() {
        System.out.println("Settings view - Coming soon!");
        // MainLayoutController.getInstance().loadContent("/fxml/SettingsView.fxml", "settings");
    }
}
