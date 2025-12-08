package com.interviewai.controller;

import com.interviewai.dao.ProgressDAO;
import com.interviewai.model.User;
import com.interviewai.util.SessionContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Controller for the Simplified Quests System
 */
public class QuestsController {

    @FXML private Label dailyQuestProgressLabel;
    @FXML private ProgressBar dailyQuestProgressBar;
    @FXML private Button dailyClaimButton;
    @FXML private Label dailyTimerLabel;

    @FXML private Label monthlyQuestProgressLabel;
    @FXML private ProgressBar monthlyQuestProgressBar;
    @FXML private Button monthlyClaimButton;
    @FXML private Label monthlyTimerLabel;

    @FXML private Label streakLabel;
    @FXML private Label totalXpLabel;

    private final ProgressDAO progressDAO = new ProgressDAO();
    private static final int DAILY_TARGET = 20;
    private static final int MONTHLY_TARGET = 12;
    private static final int DAILY_REWARD = 900;
    private static final int MONTHLY_REWARD = 30000;

    @FXML
    public void initialize() {
        loadUserData();
        startTimers();
    }

    private void loadUserData() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) return;

        try {
            int userId = currentUser.getId();

            // 1. Load Stats
            int totalXP = progressDAO.getTotalXPForUser(userId);
            int streak = progressDAO.calculateUserStreak(userId);

            totalXpLabel.setText(String.format("%,d Total XP", totalXP));
            streakLabel.setText(streak + " Day Streak");

            // 2. Load Daily Quest
            int questionsToday = progressDAO.getQuestionsAnsweredToday(userId);
            boolean dailyClaimed = progressDAO.hasClaimedDailyQuest(userId);
            
            updateDailyQuestUI(questionsToday, dailyClaimed);

            // 3. Load Monthly Quest
            int chaptersThisMonth = progressDAO.getChaptersCompletedThisMonth(userId);
            boolean monthlyClaimed = progressDAO.hasClaimedMonthlyQuest(userId);

            updateMonthlyQuestUI(chaptersThisMonth, monthlyClaimed);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading quest data: " + e.getMessage());
        }
    }

    private void updateDailyQuestUI(int current, boolean claimed) {
        double progress = Math.min(1.0, (double) current / DAILY_TARGET);
        dailyQuestProgressBar.setProgress(progress);
        dailyQuestProgressLabel.setText(current + " / " + DAILY_TARGET);

        if (claimed) {
            dailyClaimButton.setText("Claimed");
            dailyClaimButton.setDisable(true);
            if (!dailyClaimButton.getStyleClass().contains("claimed-button")) {
                dailyClaimButton.getStyleClass().add("claimed-button");
            }
        } else if (current >= DAILY_TARGET) {
            dailyClaimButton.setText("Claim Reward");
            dailyClaimButton.setDisable(false);
            dailyClaimButton.getStyleClass().remove("claimed-button");
        } else {
            dailyClaimButton.setText("In Progress");
            dailyClaimButton.setDisable(true);
            dailyClaimButton.getStyleClass().remove("claimed-button");
        }
    }

    private void updateMonthlyQuestUI(int current, boolean claimed) {
        double progress = Math.min(1.0, (double) current / MONTHLY_TARGET);
        monthlyQuestProgressBar.setProgress(progress);
        monthlyQuestProgressLabel.setText(current + " / " + MONTHLY_TARGET);

        if (claimed) {
            monthlyClaimButton.setText("Claimed");
            monthlyClaimButton.setDisable(true);
            if (!monthlyClaimButton.getStyleClass().contains("claimed-button")) {
                monthlyClaimButton.getStyleClass().add("claimed-button");
            }
        } else if (current >= MONTHLY_TARGET) {
            monthlyClaimButton.setText("Claim Reward");
            monthlyClaimButton.setDisable(false);
            monthlyClaimButton.getStyleClass().remove("claimed-button");
        } else {
            monthlyClaimButton.setText("In Progress");
            monthlyClaimButton.setDisable(true);
            monthlyClaimButton.getStyleClass().remove("claimed-button");
        }
    }

    @FXML
    private void handleClaimDaily() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) return;

        try {
            progressDAO.claimDailyQuest(currentUser.getId(), DAILY_REWARD);
            // Play animation (simple visual feedback for now)
            dailyClaimButton.setText("Claimed!");
            loadUserData(); // Refresh UI
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClaimMonthly() {
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) return;

        try {
            progressDAO.claimMonthlyQuest(currentUser.getId(), MONTHLY_REWARD);
            // Play animation
            monthlyClaimButton.setText("Claimed!");
            loadUserData(); // Refresh UI
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startTimers() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimers()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateTimers(); // Initial call
    }

    private void updateTimers() {
        LocalDateTime now = LocalDateTime.now();
        
        // Daily Timer (Midnight)
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        long secondsUntilMidnight = ChronoUnit.SECONDS.between(now, midnight);
        long hours = secondsUntilMidnight / 3600;
        long minutes = (secondsUntilMidnight % 3600) / 60;
        dailyTimerLabel.setText(String.format("Ends in %dh %dm", hours, minutes));

        // Monthly Timer (1st of next month)
        LocalDateTime nextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1).atStartOfDay();
        long daysUntilNextMonth = ChronoUnit.DAYS.between(now, nextMonth);
        monthlyTimerLabel.setText(String.format("Ends in %dd", daysUntilNextMonth));
    }
}
