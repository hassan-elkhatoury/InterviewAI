package com.interviewai.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.interviewai.dao.CourseProgressDAO;
import com.interviewai.dao.ProgressDAO;
import com.interviewai.model.Chapter;
import com.interviewai.model.GeneratedCourse;
import com.interviewai.model.User;
import com.interviewai.util.SessionContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardController {
    
    // FXML Injected Components
    @FXML private Label welcomeTitleLabel;
    @FXML private Label coachMessageLabel;
    @FXML private Label xpLabel;
    @FXML private Label streakLabel;
    @FXML private Label lessonsCompletedLabel;
    @FXML private Label interviewsLabel;
    @FXML private ProgressBar xpProgressBar;
    @FXML private Label badgeLabel;
    @FXML private Label badgeSublabel;
    
    // FXML Containers
    @FXML private HBox stageTrack;
    @FXML private VBox questBox;
    @FXML private VBox leaderboardBox;

    // Map hero + chapter detail UI
    @FXML private Label courseTitleLabel;
    @FXML private Label courseSubtitleLabel;
    @FXML private Label chapterTitleLabel;
    @FXML private Label chapterSummaryLabel;
    @FXML private Label chapterStatusLabel;
    @FXML private Label chapterProgressLabel;
    @FXML private Button startChapterButton;
    
    // DAOs
    private ProgressDAO progressDAO;
    private CourseProgressDAO courseProgressDAO;
    
    // Current user
    private User currentUser;

    // Course progress state
    private GeneratedCourse activeCourse;
    private List<Chapter> courseChapters = new ArrayList<>();
    private Chapter selectedChapter;
    
    @FXML
    public void initialize() {
        try {
            // Initialize DAO
            progressDAO = new ProgressDAO();
            courseProgressDAO = new CourseProgressDAO();
            
            // Get current user from session
            currentUser = SessionContext.getCurrentUser();
            
            if (currentUser != null) {
                if (welcomeTitleLabel != null) {
                    String username = currentUser.getUsername() != null ? currentUser.getUsername() : "there";
                    welcomeTitleLabel.setText(String.format("Welcome back, %s! \uD83D\uDC4B", username));
                }
                loadUserStats();
                loadCourseProgress();
                // loadDailyQuests(); // Disabled - questBox not in current FXML
                loadLeaderboard();
                updateCoachMessage();
            } else {
                System.err.println("No user in session!");
            }
        } catch (Exception e) {
            System.err.println("Error initializing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ===== LOAD USER STATS =====
    private void loadUserStats() {
        try {
            int userId = currentUser.getId();
            
            // Get total XP
            int totalXp = progressDAO.getTotalXPForUser(userId);
            xpLabel.setText(String.format("%,d", totalXp));
            
            // Get streak
            int streak = progressDAO.calculateUserStreak(userId);
            streakLabel.setText(String.valueOf(streak));
            
            // Set XP progress bar (scale to 0-1)
            int progressPercent = Math.min(100, (totalXp % 1000) / 10);
            xpProgressBar.setProgress(progressPercent / 100.0);
            
        } catch (SQLException e) {
            System.err.println("Error loading stats: " + e.getMessage());
        }
    }
    
    // ===== LOAD COURSE PROGRESS MAP =====
    private void loadCourseProgress() {
        try {
            stageTrack.getChildren().clear();

            var optionalCourse = courseProgressDAO.findLatestActiveCourseWithChapters(currentUser.getId());
            if (optionalCourse.isEmpty()) {
                courseTitleLabel.setText("No active course yet");
                courseSubtitleLabel.setText("Generate a course to start your journey");
                chapterTitleLabel.setText("Nothing to show");
                chapterSummaryLabel.setText("Create your first interview course to unlock the path.");
                chapterStatusLabel.setText("Locked");
                chapterProgressLabel.setText("0/0 questions completed");
                startChapterButton.setDisable(true);
                updateStatusPill(Chapter.ChapterStatus.NOT_STARTED);
                lessonsCompletedLabel.setText("0");
                interviewsLabel.setText("0");
                return;
            }

            activeCourse = optionalCourse.get();
            SessionContext.setActiveCourseId(activeCourse.getId());

            courseChapters = activeCourse.getChapters() != null ? activeCourse.getChapters() : new ArrayList<>();

            int completedChapters = (int) courseChapters.stream()
                    .filter(chapter -> chapter.getStatus() == Chapter.ChapterStatus.COMPLETED)
                    .count();
        lessonsCompletedLabel.setText(String.valueOf(completedChapters));
        interviewsLabel.setText(String.valueOf(courseChapters.size()));

        courseTitleLabel.setText(activeCourse.getCourseTitle());
        courseSubtitleLabel.setText(String.format("%d %s • %d %s complete",
            courseChapters.size(), pluralize(courseChapters.size(), "chapter", "chapters"),
            completedChapters, pluralize(completedChapters, "chapter", "chapters")));

        updateLevelBadge(completedChapters);

            if (courseChapters.isEmpty()) {
                courseSubtitleLabel.setText("No chapters available yet");
        selectedChapter = null;
        rebuildStageTrack();
        updateSelectedChapterDetails();
        return;
        }

        selectedChapter = courseChapters.stream()
            .filter(chapter -> chapter.getStatus() != Chapter.ChapterStatus.COMPLETED)
            .findFirst()
            .orElse(courseChapters.get(courseChapters.size() - 1));

            if (selectedChapter != null) {
                SessionContext.setActiveChapterId(selectedChapter.getId());
            }

            rebuildStageTrack();
            updateSelectedChapterDetails();
        } catch (SQLException e) {
            System.err.println("Error loading course progress: " + e.getMessage());
        }
    }
    
    private void rebuildStageTrack() {
        stageTrack.getChildren().clear();
        if (courseChapters.isEmpty()) {
            return;
        }

        for (int index = 0; index < courseChapters.size(); index++) {
            Chapter chapter = courseChapters.get(index);
            VBox node = createStageNode(chapter, index + 1);
            stageTrack.getChildren().add(node);

            if (index < courseChapters.size() - 1) {
                Region connector = new Region();
                connector.getStyleClass().add("stage-connector");
                connector.setMinWidth(37);
                connector.setPrefWidth(50);
                connector.setMaxWidth(70);
                stageTrack.getChildren().add(connector);
            }
        }
    }

    private VBox createStageNode(Chapter chapter, int displayNumber) {
        VBox wrapper = new VBox(8);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getStyleClass().add("stage-node");
        wrapper.setPrefWidth(145);
        wrapper.setMinWidth(132);

        StackPane circle = new StackPane();
        circle.getStyleClass().add("stage-circle");

        Chapter.ChapterStatus status = chapter.getStatus();
        switch (status) {
            case COMPLETED:
                circle.getStyleClass().add("stage-circle-complete");
                break;
            case IN_PROGRESS:
                circle.getStyleClass().add("stage-circle-active");
                break;
            default:
                circle.getStyleClass().add("stage-circle-locked");
                break;
        }

        if (Objects.equals(chapter, selectedChapter)) {
            circle.getStyleClass().add("stage-circle-selected");
        }

    Label numberLabel = new Label(String.valueOf(displayNumber));
        numberLabel.getStyleClass().add("stage-number");
        circle.getChildren().add(numberLabel);

        circle.setOnMouseClicked(event -> selectChapter(chapter));

        Label titleLabel = new Label(chapter.getName());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(130);
        titleLabel.getStyleClass().add("stage-name");

        Label progressLabel = new Label(chapter.getCompletedQuestions() + "/" + chapter.getTotalQuestions() + " questions");
        progressLabel.getStyleClass().add("stage-progress-count");

        wrapper.getChildren().addAll(circle, titleLabel, progressLabel);
        return wrapper;
    }

    private void selectChapter(Chapter chapter) {
        if (chapter == null) {
            return;
        }
        selectedChapter = chapter;
        SessionContext.setActiveChapterId(chapter.getId());
        rebuildStageTrack();
        updateSelectedChapterDetails();
    }

    private void updateSelectedChapterDetails() {
        if (selectedChapter == null) {
            chapterTitleLabel.setText("Select a chapter to view details");
            chapterSummaryLabel.setText("Pick a chapter from the path to begin.");
            chapterStatusLabel.setText("Locked");
            chapterProgressLabel.setText("0/0 questions completed");
            startChapterButton.setDisable(true);
            updateStatusPill(Chapter.ChapterStatus.NOT_STARTED);
            return;
        }

    chapterTitleLabel.setText(selectedChapter.getName());
    chapterSummaryLabel.setText(selectedChapter.getDescription() == null || selectedChapter.getDescription().isBlank()
        ? "Practice questions tailored to this topic."
        : selectedChapter.getDescription());

    Chapter.ChapterStatus status = selectedChapter.getStatus();
    chapterStatusLabel.setText(status == Chapter.ChapterStatus.COMPLETED ? "Completed" : status == Chapter.ChapterStatus.IN_PROGRESS ? "In progress" : "Ready to start");
    updateStatusPill(status);

        int total = selectedChapter.getTotalQuestions();
        int completed = selectedChapter.getCompletedQuestions();
    String questionLabel = pluralize(total, "question", "questions");
    chapterProgressLabel.setText(completed + "/" + total + " " + questionLabel + " complete");

        boolean chapterDone = status == Chapter.ChapterStatus.COMPLETED || total == 0;
        startChapterButton.setDisable(chapterDone);
        if (chapterDone) {
            startChapterButton.setText("Completed");
        } else if (status == Chapter.ChapterStatus.IN_PROGRESS) {
            startChapterButton.setText("Continue Learning");
        } else {
            startChapterButton.setText("Start Learning");
        }
    }

    private void updateStatusPill(Chapter.ChapterStatus status) {
        List<String> styles = chapterStatusLabel.getStyleClass();
        styles.removeAll(Arrays.asList("status-pill-locked", "status-pill-active", "status-pill-complete"));
        if (!styles.contains("status-pill")) {
            styles.add("status-pill");
        }

        String nextStyle;
        switch (status) {
            case COMPLETED:
                nextStyle = "status-pill-complete";
                break;
            case IN_PROGRESS:
                nextStyle = "status-pill-active";
                break;
            default:
                nextStyle = "status-pill-locked";
                break;
        }

        if (!styles.contains(nextStyle)) {
            styles.add(nextStyle);
        }
    }

    private String pluralize(int count, String singular, String plural) {
        return count == 1 ? singular : plural;
    }

    private void updateLevelBadge(int completedChapters) {
        int level = completedChapters;
        String tier;
        
        if (completedChapters == 0) {
            tier = "Beginner";
        } else if (completedChapters <= 2) {
            tier = "Getting Started";
        } else if (completedChapters <= 5) {
            tier = "Rising Star";
        } else if (completedChapters <= 10) {
            tier = "Advanced";
        } else {
            tier = "Pro Member";
        }
        
        badgeLabel.setText("LEVEL " + level);
        badgeSublabel.setText(tier);
    }

    @FXML
    private void onStartChapter() {
        if (selectedChapter == null) {
            return;
        }
        SessionContext.setActiveCourseId(activeCourse != null ? activeCourse.getId() : 0);
        SessionContext.setActiveChapterId(selectedChapter.getId());
        System.out.println("Start chapter " + selectedChapter.getChapterNumber() + " - navigate to lesson view");
        // TODO: hook into navigation when lesson scene is ready.
    }

    // ===== LOAD DAILY QUESTS =====
    private void loadDailyQuests() {
        try {
            int userId = currentUser.getId();
            List<Map<String, Object>> quests = progressDAO.getDailyQuests(userId);
            
            questBox.getChildren().clear();
            
            for (Map<String, Object> quest : quests) {
                String questName = (String) quest.get("quest_name");
                int required = ((Number) quest.get("required_count")).intValue();
                int current = ((Number) quest.get("current_count")).intValue();
                int xpReward = ((Number) quest.get("xp_reward")).intValue();
                
                VBox questItem = createQuestItem(questName, "+" + xpReward + " XP", current, required);
                questBox.getChildren().add(questItem);
            }
        } catch (SQLException e) {
            System.err.println("Error loading daily quests: " + e.getMessage());
        }
    }
    
    private VBox createQuestItem(String title, String reward, int progress, int total) {
        VBox questItem = new VBox(8);
        questItem.getStyleClass().add("quest-item");
        questItem.setStyle("-fx-padding: 12; -fx-background-color: #334155; -fx-border-radius: 8;");
        
        // Title and reward
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        Label rewardLabel = new Label(reward);
        rewardLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #22c55e; -fx-font-weight: bold;");
        
        header.getChildren().addAll(titleLabel, rewardLabel);
        
        // Progress bar
        ProgressBar progressBar = new ProgressBar((double) progress / total);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-padding: 0;");
        
        // Progress text
        Label progressLabel = new Label(progress + "/" + total);
        progressLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #94a3b8;");
        
        questItem.getChildren().addAll(header, progressBar, progressLabel);
        
        return questItem;
    }
    
    // ===== LOAD LEADERBOARD =====
    private void loadLeaderboard() {
        try {
            // Get top 5 learners
            List<Map<String, Object>> topUsers = progressDAO.getTopLearners(5);
            
            leaderboardBox.getChildren().clear();
            
            int rank = 1;
            for (Map<String, Object> user : topUsers) {
                String username = (String) user.getOrDefault("username", "Unknown");
                int xp = ((Number) user.getOrDefault("total_xp", 0)).intValue();
                
                HBox row = createLeaderboardItem(String.valueOf(rank), username, String.format("%,d", xp) + " XP");
                leaderboardBox.getChildren().add(row);
                rank++;
            }
        } catch (SQLException e) {
            System.err.println("Error loading leaderboard: " + e.getMessage());
        }
    }
    
    private HBox createLeaderboardItem(String rank, String name, String xp) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 10; -fx-background-color: #334155; -fx-border-radius: 6;");
        
        Label rankLabel = new Label(rank);
        rankLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-min-width: 30;");
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 12;");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        Label xpLabel = new Label(xp);
        xpLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #22c55e; -fx-font-weight: bold;");
        
        item.getChildren().addAll(rankLabel, nameLabel, xpLabel);
        
        return item;
    }
    
    // ===== UPDATE AI COACH MESSAGE =====
    private void updateCoachMessage() {
        try {
            int userId = currentUser.getId();
            int totalXp = progressDAO.getTotalXPForUser(userId);
            int streak = progressDAO.calculateUserStreak(userId);
            
            String message = generateCoachMessage(totalXp, streak);
            coachMessageLabel.setText(message);
        } catch (SQLException e) {
            coachMessageLabel.setText("Keep learning, you're doing great! 💪");
        }
    }
    
    private String generateCoachMessage(int xp, int streak) {
        if (streak >= 10) {
            return "Incredible dedication! 🔥 Your " + streak + "-day streak is impressive. Keep this momentum going!";
        } else if (streak >= 5) {
            return "You're on fire! 🎯 " + streak + " days of consistency. Let's keep the streak alive!";
        } else if (xp >= 500) {
            return "Amazing progress! You've earned " + xp + " XP. Let's push for 1000! 💪";
        } else if (xp >= 250) {
            return "Great job so far! 👏 You're at " + xp + " XP. Keep going!";
        } else {
            return "Welcome! Start with today's lessons and build your interview skills. You've got this! 🚀";
        }
    }
    
    // ===== NAVIGATION EVENT HANDLERS =====
    @FXML private void onOpenHome() { System.out.println("Home clicked"); }
    @FXML private void onOpenProgress() { System.out.println("Progress clicked"); }
    @FXML private void onOpenQuests() { System.out.println("Quests clicked"); }
    @FXML private void onOpenLeaderboards() { System.out.println("Leaderboard clicked"); }
    @FXML private void onOpenProfile() { System.out.println("Profile clicked"); }
    @FXML private void onOpenSettings() { System.out.println("Settings clicked"); }
    
    // ===== ACTION EVENT HANDLERS =====
    @FXML private void onViewAllQuests() { System.out.println("View all quests clicked"); }
}