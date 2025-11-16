package com.interviewai.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Controller for the Progress Overview page
 * Displays user statistics, XP, goals, and course/chapter progress with charts
 * NOTE: This controller uses PLACEHOLDER data for demonstration purposes
 */
public class ProgressController implements Initializable {

    // FXML Components - Header
    @FXML private Label progressTitleLabel;
    @FXML private Label progressSubtitleLabel;
    @FXML private Label xpLabel;
    @FXML private Label streakLabel;
    @FXML private Label accuracyLabel;

    // FXML Components - Charts
    @FXML private LineChart<String, Number> xpChart;
    @FXML private BarChart<String, Number> streakChart;

    // FXML Components - Overall Progress
    @FXML private Label questionsAnsweredLabel;
    @FXML private ProgressBar questionsProgressBar;
    @FXML private Label timeSpentLabel;
    @FXML private ProgressBar timeProgressBar;
    @FXML private Label coursesEnrolledLabel;
    @FXML private ProgressBar coursesProgressBar;

    // FXML Components - Goals
    @FXML private Button editGoalsBtn;
    @FXML private Label goal1ProgressLabel;
    @FXML private ProgressBar goal1ProgressBar;
    @FXML private Label goal2ProgressLabel;
    @FXML private ProgressBar goal2ProgressBar;
    @FXML private Label goal3ProgressLabel;
    @FXML private ProgressBar goal3ProgressBar;

    // FXML Components - Courses
    @FXML private VBox courseProgressContainer;
    @FXML private Label course1ProgressLabel;
    @FXML private ProgressBar course1ProgressBar;
    @FXML private Label course2ProgressLabel;
    @FXML private ProgressBar course2ProgressBar;
    @FXML private Label course3ProgressLabel;
    @FXML private ProgressBar course3ProgressBar;

    // FXML Components - Bottom Bar
    @FXML private Button backToDashboardBtn;
    @FXML private Label motivationLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        loadPlaceholderData();
        setupCharts();
        activateProgressSidebarButton();
    }

    /**
     * Activate the progress button in sidebar
     */
    private void activateProgressSidebarButton() {
        javafx.application.Platform.runLater(() -> {
            MainLayoutController mainLayout = MainLayoutController.getInstance();
            if (mainLayout != null && mainLayout.getSidebarController() != null) {
                mainLayout.getSidebarController().setActiveButton("progress");
            }
        });
    }

    /**
     * Setup event handlers for buttons
     */
    private void setupEventHandlers() {
        // Edit Goals button
        if (editGoalsBtn != null) {
            editGoalsBtn.setOnAction(e -> onEditGoals());
        }

        // Back to Dashboard button
        if (backToDashboardBtn != null) {
            backToDashboardBtn.setOnAction(e -> onBackToDashboard());
        }
    }

    /**
     * Setup charts with placeholder data
     */
    private void setupCharts() {
        setupXPChart();
        setupStreakChart();
    }

    /**
     * Setup XP Line Chart with placeholder data
     */
    private void setupXPChart() {
        if (xpChart == null) return;

        // Clear any existing data
        xpChart.getData().clear();

        // Create series for XP data
        XYChart.Series<String, Number> xpSeries = new XYChart.Series<>();
        xpSeries.setName("Daily XP");

        // Placeholder data for last 7 days
        xpSeries.getData().add(new XYChart.Data<>("Mon", 320));
        xpSeries.getData().add(new XYChart.Data<>("Tue", 280));
        xpSeries.getData().add(new XYChart.Data<>("Wed", 420));
        xpSeries.getData().add(new XYChart.Data<>("Thu", 380));
        xpSeries.getData().add(new XYChart.Data<>("Fri", 450));
        xpSeries.getData().add(new XYChart.Data<>("Sat", 290));
        xpSeries.getData().add(new XYChart.Data<>("Sun", 310));

        // Add series to chart
        xpChart.getData().add(xpSeries);
        
        // Styling
        xpChart.setLegendVisible(false);
        xpChart.setAnimated(false);
    }

    /**
     * Setup Streak Bar Chart with placeholder data
     */
    private void setupStreakChart() {
        if (streakChart == null) return;

        // Clear any existing data
        streakChart.getData().clear();

        // Create series for streak data
        XYChart.Series<String, Number> streakSeries = new XYChart.Series<>();
        streakSeries.setName("Minutes Studied");

        // Placeholder data for last 7 days (minutes studied per day)
        streakSeries.getData().add(new XYChart.Data<>("Mon", 45));
        streakSeries.getData().add(new XYChart.Data<>("Tue", 30));
        streakSeries.getData().add(new XYChart.Data<>("Wed", 60));
        streakSeries.getData().add(new XYChart.Data<>("Thu", 50));
        streakSeries.getData().add(new XYChart.Data<>("Fri", 75));
        streakSeries.getData().add(new XYChart.Data<>("Sat", 40));
        streakSeries.getData().add(new XYChart.Data<>("Sun", 35));

        // Add series to chart
        streakChart.getData().add(streakSeries);
        
        // Styling
        streakChart.setLegendVisible(false);
        streakChart.setAnimated(false);
    }

    /**
     * Load placeholder data for demonstration
     * In production, this would fetch real data from the database
     */
    private void loadPlaceholderData() {
        // Header Stats - Placeholder Data
        if (xpLabel != null) {
            xpLabel.setText("2,450");
        }
        if (streakLabel != null) {
            streakLabel.setText("12");
        }
        if (accuracyLabel != null) {
            accuracyLabel.setText("87%");
        }

        // Overall Progress - Placeholder Data
        if (questionsAnsweredLabel != null) {
            questionsAnsweredLabel.setText("245 / 320");
        }
        if (questionsProgressBar != null) {
            questionsProgressBar.setProgress(0.77); // 77%
        }
        if (timeSpentLabel != null) {
            timeSpentLabel.setText("24h 35m");
        }
        if (timeProgressBar != null) {
            timeProgressBar.setProgress(0.65); // 65%
        }
        if (coursesEnrolledLabel != null) {
            coursesEnrolledLabel.setText("3 Courses");
        }
        if (coursesProgressBar != null) {
            coursesProgressBar.setProgress(0.45); // 45%
        }

        // Goals - Placeholder Data
        if (goal1ProgressLabel != null) {
            goal1ProgressLabel.setText("75%");
        }
        if (goal1ProgressBar != null) {
            goal1ProgressBar.setProgress(0.75);
        }
        if (goal2ProgressLabel != null) {
            goal2ProgressLabel.setText("87%");
        }
        if (goal2ProgressBar != null) {
            goal2ProgressBar.setProgress(0.87);
        }
        if (goal3ProgressLabel != null) {
            goal3ProgressLabel.setText("40%");
        }
        if (goal3ProgressBar != null) {
            goal3ProgressBar.setProgress(0.40);
        }

        // Courses - Placeholder Data
        if (course1ProgressLabel != null) {
            course1ProgressLabel.setText("67%");
        }
        if (course1ProgressBar != null) {
            course1ProgressBar.setProgress(0.67);
        }
        if (course2ProgressLabel != null) {
            course2ProgressLabel.setText("30%");
        }
        if (course2ProgressBar != null) {
            course2ProgressBar.setProgress(0.30);
        }
        if (course3ProgressLabel != null) {
            course3ProgressLabel.setText("13%");
        }
        if (course3ProgressBar != null) {
            course3ProgressBar.setProgress(0.13);
        }

        // Motivation message
        if (motivationLabel != null) {
            String[] motivations = {
                "Keep up the great work! 🚀",
                "You're making amazing progress! 💪",
                "Stay consistent and you'll succeed! ⭐",
                "Learning journey in full swing! 🎯",
                "Every question answered is progress! 📈"
            };
            int randomIndex = (int) (Math.random() * motivations.length);
            motivationLabel.setText(motivations[randomIndex]);
        }
    }

    /**
     * Handle Edit Goals button click
     */
    private void onEditGoals() {
        System.out.println("Edit Goals clicked - Feature not yet implemented");
        
        // TODO: Open goals editor dialog or navigate to goals page
        // For now, just log the action
        
        // In production, this would:
        // 1. Open a dialog to edit user goals
        // 2. Update goals in database
        // 3. Refresh the progress view
    }

    /**
     * Navigate back to dashboard
     */
    private void onBackToDashboard() {
        System.out.println("Navigating back to dashboard");
        
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadContent("/fxml/DashboardContent.fxml", "dashboard");
        }
    }

    /**
     * Refresh progress data
     * Call this method when returning to the progress page to update stats
     */
    public void refreshProgressData() {
        System.out.println("Refreshing progress data");
        
        // TODO: In production, fetch latest data from database
        // For now, just reload placeholder data
        loadPlaceholderData();
        setupCharts();
    }

    /**
     * Update XP display and chart
     * @param newXP The new XP value to display
     */
    public void updateXP(int newXP) {
        if (xpLabel != null) {
            xpLabel.setText(String.valueOf(newXP));
        }
        // TODO: Update XP chart with new data
    }

    /**
     * Update streak display and chart
     * @param newStreak The new streak value to display
     */
    public void updateStreak(int newStreak) {
        if (streakLabel != null) {
            streakLabel.setText(String.valueOf(newStreak));
        }
        // TODO: Update streak chart with new data
    }

    /**
     * Update accuracy display
     * @param newAccuracy The new accuracy percentage to display
     */
    public void updateAccuracy(double newAccuracy) {
        if (accuracyLabel != null) {
            accuracyLabel.setText(String.format("%.0f%%", newAccuracy));
        }
    }

    /**
     * Add new XP data point to chart
     * @param day The day label
     * @param xpValue The XP value earned
     */
    public void addXPDataPoint(String day, int xpValue) {
        if (xpChart != null && !xpChart.getData().isEmpty()) {
            XYChart.Series<String, Number> series = xpChart.getData().get(0);
            
            // Remove oldest data point if we have more than 7
            if (series.getData().size() >= 7) {
                series.getData().remove(0);
            }
            
            // Add new data point
            series.getData().add(new XYChart.Data<>(day, xpValue));
        }
    }

    /**
     * Add new streak data point to chart
     * @param day The day label
     * @param minutes Minutes studied
     */
    public void addStreakDataPoint(String day, int minutes) {
        if (streakChart != null && !streakChart.getData().isEmpty()) {
            XYChart.Series<String, Number> series = streakChart.getData().get(0);
            
            // Remove oldest data point if we have more than 7
            if (series.getData().size() >= 7) {
                series.getData().remove(0);
            }
            
            // Add new data point
            series.getData().add(new XYChart.Data<>(day, minutes));
        }
    }
}