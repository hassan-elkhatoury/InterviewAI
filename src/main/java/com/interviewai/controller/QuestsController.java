package com.interviewai.controller;

import com.interviewai.dao.ProgressDAO;
import com.interviewai.model.User;
import com.interviewai.util.SessionContext;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Quests & Challenges view
 */
public class QuestsController {

    @FXML private Label monthlyChallengeTitle;
    @FXML private Label monthlyChallengeProgressLabel;
    @FXML private ProgressBar monthlyChallengeProgressBar;
    @FXML private VBox dailyQuestsContainer;
    @FXML private Label streakLabel;
    @FXML private Label totalXpLabel;

    private final ProgressDAO progressDAO = new ProgressDAO();

    @FXML
    public void initialize() {
        loadUserData();
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

            // 2. Load Monthly Challenge (Real Data)
            // Challenge: Answer 100 questions correctly this month
            int correctThisMonth = progressDAO.getQuestionsAnsweredCorrectlyThisMonth(userId);
            int targetQuestions = 100;
            
            double monthlyProgress = Math.min(1.0, (double) correctThisMonth / targetQuestions);
            
            monthlyChallengeProgressLabel.setText(Math.min(correctThisMonth, targetQuestions) + " / " + targetQuestions);
            monthlyChallengeProgressBar.setProgress(monthlyProgress);

            // 3. Load Daily Quests
            List<Map<String, Object>> quests = progressDAO.getDailyQuests(userId);
            dailyQuestsContainer.getChildren().clear();
            
            for (Map<String, Object> quest : quests) {
                String title = (String) quest.get("quest_name");
                int required = (int) quest.get("required_count");
                int current = (int) quest.get("current_count");
                
                // Determine type for icon/color based on title keywords
                String type = "xp";
                String emoji = "⚡";
                if (title.toLowerCase().contains("streak")) {
                    type = "streak";
                    emoji = "🔥";
                } else if (title.toLowerCase().contains("accuracy") || title.toLowerCase().contains("score")) {
                    type = "accuracy";
                    emoji = "🎯";
                } else if (title.toLowerCase().contains("lesson") || title.toLowerCase().contains("course")) {
                    type = "lesson";
                    emoji = "📚";
                }

                dailyQuestsContainer.getChildren().add(createQuestCard(title, current, required, type, emoji));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading quest data: " + e.getMessage());
        }
    }

    private HBox createQuestCard(String title, int current, int required, String type, String emoji) {
        HBox card = new HBox();
        card.getStyleClass().add("quest-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(20);

        // Icon Container
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().addAll("quest-icon-container", "icon-" + type);
        Label emojiLabel = new Label(emoji);
        emojiLabel.getStyleClass().add("quest-emoji");
        iconContainer.getChildren().add(emojiLabel);

        // Content
        VBox content = new VBox();
        content.setSpacing(8);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("quest-title");
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().addAll("quest-progress-bar", "bar-" + type);
        double progress = (double) current / required;
        progressBar.setProgress(Math.min(1.0, progress));
        
        content.getChildren().addAll(titleLabel, progressBar);

        // Reward Box
        VBox rewardBox = new VBox();
        rewardBox.getStyleClass().add("reward-box");
        
        Label rewardIcon = new Label();
        rewardIcon.getStyleClass().add("reward-icon");
        
        if (current >= required) {
            rewardBox.setStyle("-fx-background-color: #FACC15;");
            rewardIcon.setText("✅");
            rewardIcon.setStyle("-fx-text-fill: black;");
        } else {
            rewardIcon.setText("🎁");
        }
        
        rewardBox.getChildren().add(rewardIcon);

        card.getChildren().addAll(iconContainer, content, rewardBox);
        return card;
    }
}