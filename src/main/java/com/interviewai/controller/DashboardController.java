package com.interviewai.controller;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.interviewai.dao.CourseProgressDAO;
import com.interviewai.dao.InterviewPrepDAO;
import com.interviewai.dao.ProgressDAO;
import com.interviewai.model.Chapter;
import com.interviewai.model.GeneratedCourse;
import com.interviewai.model.User;
import com.interviewai.util.SessionContext;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardController {
    
    // FXML Injected Components
    @FXML private Label welcomeTitleLabel;
    @FXML private MenuButton courseMenuButton;
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
    @FXML private Label chapterProgressLabel;
    @FXML private Button startChapterButton;
    @FXML private ProgressBar chapterProgressBar; // progress bar for chapter question completion
    
    // DAOs
    private ProgressDAO progressDAO;
    private CourseProgressDAO courseProgressDAO;
    private InterviewPrepDAO interviewPrepDAO;
    
    // Current user
    private User currentUser;

    // Course progress state
    private GeneratedCourse activeCourse;
    private List<Chapter> courseChapters = new ArrayList<>();
    private Chapter selectedChapter;
    
    // Course type tracking
    private int currentTechnicalCourseId = -1;
    private int currentSoftskillsCourseId = -1;
    private boolean isViewingTechnical = true; // Track which type is currently displayed
    
    @FXML
    public void initialize() {
        try {
            // Initialize DAOs
            progressDAO = new ProgressDAO();
            courseProgressDAO = new CourseProgressDAO();
            interviewPrepDAO = new InterviewPrepDAO();
            
            // Get current user from session
            currentUser = SessionContext.getCurrentUser();
            
            if (currentUser != null) {
                // Set username in welcome label
                if (welcomeTitleLabel != null) {
                    String username = currentUser.getUsername() != null ? currentUser.getUsername() : "User";
                    welcomeTitleLabel.setText(String.format("Welcome back, %s! 👋", username));
                }
                
                // Load user's course IDs
                loadUserCourseIds();
                
                // If we have a last-used course set in session, prefer it and set view type accordingly
                Integer lastCourseId = SessionContext.getActiveCourseId();
                if (lastCourseId != null && lastCourseId > 0) {
                    // Determine if lastCourseId is technical or soft-skills to set the toggle state
                    if (lastCourseId == currentTechnicalCourseId) {
                        isViewingTechnical = true;
                    } else if (lastCourseId == currentSoftskillsCourseId) {
                        isViewingTechnical = false;
                    }
                    // Switch to last used course
                    switchToCourse(lastCourseId);
                }

                // Update course menu
                updateCourseMenu();
                
                loadUserStats();
                loadCourseProgress();
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
    
    /**
     * Load the user's technical and soft-skills course IDs from the database
     */
    private void loadUserCourseIds() {
        int userId = currentUser.getId();
        // Strategy: select the pair whose technical OR soft-skills course matches the last used course
        Integer lastUsedCourseId = SessionContext.getActiveCourseId();
        var allPreps = interviewPrepDAO.getAllInterviewPreps(userId);
        int selectedTech = -1;
        int selectedSoft = -1;
        if (lastUsedCourseId != null && lastUsedCourseId > 0) {
            for (var prep : allPreps) {
                if (prep.technicalCourseId == lastUsedCourseId || prep.softskillsCourseId == lastUsedCourseId) {
                    selectedTech = prep.technicalCourseId;
                    selectedSoft = prep.softskillsCourseId;
                    break;
                }
            }
        }
        // Fallback: latest prep pair
        if (selectedTech == -1 && !allPreps.isEmpty()) {
            selectedTech = allPreps.get(0).technicalCourseId;
            selectedSoft = allPreps.get(0).softskillsCourseId;
        }
        // As final fallback, use previous single fetch behavior
        if (selectedTech == -1) {
            selectedTech = interviewPrepDAO.getTechnicalCourseId(userId);
        }
        if (selectedSoft == -1) {
            selectedSoft = interviewPrepDAO.getSoftskillsCourseId(userId);
        }
        currentTechnicalCourseId = selectedTech;
        currentSoftskillsCourseId = selectedSoft;
        
        System.out.println("Loaded course IDs - Technical: " + currentTechnicalCourseId + 
                         ", Soft-skills: " + currentSoftskillsCourseId);
    }
    
    /**
     * Update the course menu to show which type is active
     */
    private void updateCourseMenu() {
        if (courseMenuButton == null) return;
        
        // Update menu button text to show active course type
        String menuText = isViewingTechnical ? "⚙ Technical" : "⚙ Soft Skills";
        courseMenuButton.setText(menuText);
        
        // Update menu items to show checkmark for active type
        courseMenuButton.getItems().clear();
        
        MenuItem technicalItem = new MenuItem(isViewingTechnical ? "✓ 🎯 Technical" : "🎯 Technical");
        technicalItem.setOnAction(e -> onSelectTechnical());
        if (isViewingTechnical) {
            technicalItem.setStyle("-fx-background-color: rgba(59, 130, 246, 0.2); -fx-font-weight: bold;");
        }
        
        MenuItem softSkillsItem = new MenuItem(!isViewingTechnical ? "✓ 💬 Soft Skills" : "💬 Soft Skills");
        softSkillsItem.setOnAction(e -> onSelectSoftSkills());
        if (!isViewingTechnical) {
            softSkillsItem.setStyle("-fx-background-color: rgba(59, 130, 246, 0.2); -fx-font-weight: bold;");
        }
        
        courseMenuButton.getItems().addAll(
            technicalItem,
            softSkillsItem,
            new javafx.scene.control.SeparatorMenuItem(),
            createMenuItem("➕ New Course", this::onCreateNewCourse),
            createMenuItem("⇄ Switch Course", this::onSwitchCourse)
        );
    }
    
    /**
     * Helper method to create a menu item with action
     */
    private MenuItem createMenuItem(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        return item;
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

            // If activeCourse is not set, prefer session's last used course; else fetch the latest
            if (activeCourse == null) {
                Integer preferredCourseId = SessionContext.getActiveCourseId();
                if (preferredCourseId != null && preferredCourseId > 0) {
                    GeneratedCourse preferred = courseProgressDAO.getCourseById(preferredCourseId);
                    if (preferred != null) {
                        activeCourse = preferred;
                    }
                }
            }
            if (activeCourse == null) {
                var optionalCourse = courseProgressDAO.findLatestActiveCourseWithChapters(currentUser.getId());
                if (optionalCourse.isEmpty()) {
                    showNoCourseState();
                    return;
                }
                activeCourse = optionalCourse.get();
            }
            
            // Ensure we have the full course data with chapters
            if (activeCourse.getChapters() == null || activeCourse.getChapters().isEmpty()) {
                GeneratedCourse fullCourse = courseProgressDAO.getCourseById(activeCourse.getId());
                if (fullCourse != null) {
                    activeCourse = fullCourse;
                }
            }
            
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

            // Auto-select first incomplete (unlocked) or last if all completed
            Chapter firstIncomplete = courseChapters.stream()
                .filter(ch -> ch.getStatus() != Chapter.ChapterStatus.COMPLETED)
                .findFirst()
                .orElse(null);
            if (firstIncomplete != null) {
                selectedChapter = firstIncomplete;
            } else {
                selectedChapter = courseChapters.get(courseChapters.size() - 1); // all completed
            }

            if (selectedChapter != null) {
                SessionContext.setActiveChapterId(selectedChapter.getId());
            }

            rebuildStageTrack();
            updateSelectedChapterDetails();
        } catch (SQLException e) {
            System.err.println("Error loading course progress: " + e.getMessage());
        }
    }
    
    /**
     * Show the state when no course is available
     */
    private void showNoCourseState() {
        courseTitleLabel.setText("No active course yet");
        courseSubtitleLabel.setText("Generate a course to start your journey");
        chapterTitleLabel.setText("Nothing to show");
        chapterSummaryLabel.setText("Create your first interview course to unlock the path.");
        chapterProgressLabel.setText("0/0 questions completed");
        startChapterButton.setDisable(true);
        lessonsCompletedLabel.setText("0");
        interviewsLabel.setText("0");
    }
    
    private void rebuildStageTrack() {
        stageTrack.getChildren().clear();
        if (courseChapters.isEmpty()) {
            return;
        }

        // Refresh counts for all chapters to keep mini labels in sync with DB
        if (courseProgressDAO != null) {
            for (Chapter ch : courseChapters) {
                try {
                    var counts = courseProgressDAO.refreshChapterCounts(ch.getId());
                    ch.setTotalQuestions(counts.getOrDefault("total", ch.getTotalQuestions()));
                    ch.setCompletedQuestions(counts.getOrDefault("completed", ch.getCompletedQuestions()));
                } catch (SQLException e) {
                    // Log and continue; stale numbers are acceptable temporarily
                    System.err.println("Failed to refresh counts for chapter " + ch.getId() + ": " + e.getMessage());
                }
            }
        }

        // Gating: only completed chapters and the first incomplete chapter are unlocked
        boolean firstIncompleteSeen = false;

        for (int index = 0; index < courseChapters.size(); index++) {
            Chapter chapter = courseChapters.get(index);

            boolean canClick;
            if (chapter.getStatus() == Chapter.ChapterStatus.COMPLETED) {
                canClick = true;
            } else if (!firstIncompleteSeen) {
                canClick = true;
                firstIncompleteSeen = true; // only the first incomplete is unlocked
            } else {
                canClick = false; // locked until previous completed
            }

            VBox node = createStageNode(chapter, index + 1, canClick);
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

    private VBox createStageNode(Chapter chapter, int displayNumber, boolean canClick) {
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

        if (canClick) {
            circle.setOnMouseClicked(event -> selectChapter(chapter));
        } else {
            // Visually indicate locked by reducing opacity and disabling mouse
            wrapper.setOpacity(0.65);
            circle.setMouseTransparent(true);
        }

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

        // Enforce gating: prevent selecting chapters beyond the first incomplete
        if (!isChapterUnlocked(chapter)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chapter Locked");
            alert.setHeaderText("Complete previous chapter first");
            alert.setContentText("Please complete the previous chapter before unlocking this one.");
            alert.showAndWait();
            return;
        }
        selectedChapter = chapter;
        SessionContext.setActiveChapterId(chapter.getId());
        // Persist last-used (both course and chapter) for this user
        try {
            if (currentUser != null && activeCourse != null) {
                new com.interviewai.dao.UserDAO().updateLastUsed(currentUser.getId(), activeCourse.getId(), chapter.getId());
            }
        } catch (Exception e) {
            System.err.println("Failed to update last-used: " + e.getMessage());
        }
        rebuildStageTrack();
        updateSelectedChapterDetails();

        // Update lesson button visibility in sidebar
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null && mainLayout.getSidebarController() != null) {
            mainLayout.getSidebarController().updateLessonButtonVisibility();
        }
    }

    /**
     * Return true if the chapter is selectable: completed, or the first incomplete chapter.
     */
    private boolean isChapterUnlocked(Chapter chapter) {
        int firstIncompleteIndex = -1;
        for (int i = 0; i < courseChapters.size(); i++) {
            if (courseChapters.get(i).getStatus() != Chapter.ChapterStatus.COMPLETED) {
                firstIncompleteIndex = i;
                break;
            }
        }
        int idx = courseChapters.indexOf(chapter);
        if (idx < 0) return false;
        if (chapter.getStatus() == Chapter.ChapterStatus.COMPLETED) return true;
        // If all completed, nothing to unlock specifically, but allow selection
        if (firstIncompleteIndex == -1) return true;
        return idx == firstIncompleteIndex;
    }

    private void updateSelectedChapterDetails() {
        if (selectedChapter == null) {
            chapterTitleLabel.setText("Select a chapter to view details");
            chapterSummaryLabel.setText("Pick a chapter from the path to begin.");
            chapterProgressLabel.setText("0/0 questions completed");
            if (chapterProgressBar != null) {
                chapterProgressBar.setProgress(0);
            }
            startChapterButton.setDisable(true);
            return;
        }

    chapterTitleLabel.setText(selectedChapter.getName());
    chapterSummaryLabel.setText(selectedChapter.getDescription() == null || selectedChapter.getDescription().isBlank()
        ? "Practice questions tailored to this topic."
        : selectedChapter.getDescription());

    Chapter.ChapterStatus status = selectedChapter.getStatus();

        // Always refresh counts from DB for dynamic accuracy
        int total = selectedChapter.getTotalQuestions();
        int completed = selectedChapter.getCompletedQuestions();
        if (courseProgressDAO != null) {
            try {
                var counts = courseProgressDAO.refreshChapterCounts(selectedChapter.getId());
                total = counts.getOrDefault("total", total);
                completed = counts.getOrDefault("completed", completed);
                // update cached values on selectedChapter
                selectedChapter.setTotalQuestions(total);
                selectedChapter.setCompletedQuestions(completed);
            } catch (SQLException e) {
                System.err.println("Failed to refresh chapter counts: " + e.getMessage());
            }
        }
    String questionLabel = pluralize(total, "question", "questions");
    double pct = total == 0 ? 0.0 : (completed * 100.0 / total);
    chapterProgressLabel.setText(String.format("%d/%d %s (%.0f%%)", completed, total, questionLabel, pct));
        if (chapterProgressBar != null) {
            chapterProgressBar.setProgress(total == 0 ? 0 : (completed / (double) total));
        }

    boolean chapterDone = status == Chapter.ChapterStatus.COMPLETED || total == 0;
    boolean unlocked = isChapterUnlocked(selectedChapter);
    startChapterButton.setDisable(chapterDone || !unlocked);
    
    // Set button label based on chapter state
    String buttonText;
    if (!unlocked && !chapterDone) {
        buttonText = "Locked";
    } else if (chapterDone) {
        buttonText = "Completed";
    }else {
        buttonText = "Start Learning";
    }
    
    // Update button with icon and text
    HBox buttonContent = new HBox(8);
    buttonContent.setAlignment(Pos.CENTER);
    Label icon = new Label("▶ ");
    icon.getStyleClass().add("btn-icon");
    Label text = new Label(buttonText);
    text.getStyleClass().add("btn-text");
    buttonContent.getChildren().addAll(icon, text);
    startChapterButton.setGraphic(buttonContent);
    startChapterButton.setText("");
    }

    /**
     * Public hook to be called after answering a question or returning from lesson view
     * to ensure the dashboard reflects the latest progress for the selected chapter.
     */
    public void refreshSelectedChapterProgress() {
        if (selectedChapter == null) return;
        updateSelectedChapterDetails();
        rebuildStageTrack(); // also update mini progress numbers on nodes
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
        
        // Navigate to lesson view using MainLayout (keeps sidebar persistent)
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadContent("/fxml/LessonView.fxml", "lesson");
            
            // Ensure sidebar lesson button is activated after loading
            javafx.application.Platform.runLater(() -> {
                if (mainLayout.getSidebarController() != null) {
                    mainLayout.getSidebarController().setActiveButton("lesson");
                    System.out.println("✓ Sidebar lesson button activated from dashboard");
                }
            });
        }
    }
    
    // ===== LOAD LEADERBOARD =====
    private void loadLeaderboard() {
        try {
            // Get top 5 learners for dashboard preview
            List<Map<String, Object>> topUsers = progressDAO.getTopLearners(5);
            
            System.out.println("Loading leaderboard - found " + topUsers.size() + " users");
            
            leaderboardBox.getChildren().clear();
            
            int rank = 1;
            for (Map<String, Object> user : topUsers) {
                String username = (String) user.getOrDefault("username", "Unknown");
                int xpValue = ((Number) user.getOrDefault("total_xp", 0)).intValue();
                
                System.out.println("User #" + rank + ": " + username + " - XP: " + xpValue);
                
                HBox row = createLeaderboardItem(String.valueOf(rank), username, String.format("%,d", xpValue));
                leaderboardBox.getChildren().add(row);
                rank++;
            }
        } catch (SQLException e) {
            System.err.println("Error loading leaderboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private HBox createLeaderboardItem(String rank, String name, String xp) {
        HBox item = new HBox(16);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("leaderboard-item");
        item.setMaxWidth(Double.MAX_VALUE);
        item.setPrefWidth(Double.MAX_VALUE);
        
        int rankNum = Integer.parseInt(rank);
        
        // Rank badge with medal icons for top 3
        VBox rankContainer = new VBox(2);
        rankContainer.setAlignment(Pos.CENTER);
        rankContainer.setMinWidth(50);
        rankContainer.setMaxWidth(50);
        
        Label rankBadge = new Label();
        rankBadge.getStyleClass().add("leaderboard-rank-badge");
        
        if (rankNum == 1) {
            rankBadge.setText("🥇");
            rankBadge.getStyleClass().add("rank-gold");
        } else if (rankNum == 2) {
            rankBadge.setText("🥈");
            rankBadge.getStyleClass().add("rank-silver");
        } else if (rankNum == 3) {
            rankBadge.setText("🥉");
            rankBadge.getStyleClass().add("rank-bronze");
        } else {
            rankBadge.setText("#" + rank);
            rankBadge.getStyleClass().add("rank-number");
        }
        
        rankContainer.getChildren().add(rankBadge);
        
        // Avatar circle with first letter
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("leaderboard-avatar");
        avatar.setMinSize(40, 40);
        avatar.setMaxSize(40, 40);
        
        Label initial = new Label(name.substring(0, 1).toUpperCase());
        initial.getStyleClass().add("avatar-initial");
        avatar.getChildren().add(initial);
        
        // Name and rank position
        VBox nameContainer = new VBox(2);
        nameContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(nameContainer, Priority.ALWAYS);
        
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("leaderboard-name");
        
        Label positionLabel = new Label("Rank #" + rank);
        positionLabel.getStyleClass().add("leaderboard-position");
        
        nameContainer.getChildren().addAll(nameLabel, positionLabel);
        
        // XP display with progress indicator
        VBox xpContainer = new VBox(4);
        xpContainer.setAlignment(Pos.CENTER_RIGHT);
        xpContainer.setMinWidth(80);
        
        HBox xpDisplay = new HBox(4);
        xpDisplay.setAlignment(Pos.CENTER_RIGHT);
        
        Label xpIcon = new Label("⚡");
        xpIcon.getStyleClass().add("xp-icon");
        
        Label xpValue = new Label(xp);
        xpValue.getStyleClass().add("leaderboard-xp-value");
        
        xpDisplay.getChildren().addAll(xpIcon, xpValue);
        
        // Progress bar showing relative XP
        ProgressBar xpBar = new ProgressBar();
        xpBar.getStyleClass().add("leaderboard-xp-bar");
        xpBar.setPrefWidth(70);
        xpBar.setPrefHeight(4);
        
        // Calculate progress relative to top player (if this is top, show 100%)
        double progress = rankNum == 1 ? 1.0 : Math.max(0.3, 1.0 - (rankNum * 0.15));
        xpBar.setProgress(progress);
        
        xpContainer.getChildren().addAll(xpDisplay, xpBar);
        
        item.getChildren().addAll(rankContainer, avatar, nameContainer, xpContainer);
        
        // Add special styling for top 3
        if (rankNum <= 3) {
            item.getStyleClass().add("leaderboard-top-3");
        }
        
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
    @FXML private void onOpenProfile() { System.out.println("Profile clicked"); }
    @FXML private void onOpenSettings() { System.out.println("Settings clicked"); }
    
    // ===== COURSE SELECTOR EVENT HANDLERS =====
    
    @FXML
    private void onCreateNewCourse() {
        System.out.println("Create new course clicked - launching onboarding flow");
        // Set flag so onboarding knows we came from dashboard
        SessionContext.setOnboardingFromDashboard(true);
        try {
            // Switch root scene to onboarding flow
            Stage stage = (Stage) startChapterButton.getScene().getWindow();
            com.interviewai.util.SceneNavigator.switchTo(stage, com.interviewai.util.Routes.ONBOARDING, stage.getWidth()-15, stage.getHeight()-38);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to Open Onboarding");
            alert.setContentText("Could not open the onboarding flow: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void onSelectTechnical() {
        System.out.println("Switching to Technical course");
        
        if (currentTechnicalCourseId <= 0) {
            showNoCourseAlert("Technical");
            return;
        }
        
        isViewingTechnical = true;
        // Ensure we switch to the technical course in the currently selected pair only
        switchToCourse(currentTechnicalCourseId);
        updateCourseMenu();
    }
    
    @FXML
    private void onSelectSoftSkills() {
        System.out.println("Switching to Soft Skills course");
        
        if (currentSoftskillsCourseId <= 0) {
            showNoCourseAlert("Soft Skills");
            return;
        }
        
        isViewingTechnical = false;
        // Ensure we switch to the soft-skills course in the currently selected pair only
        switchToCourse(currentSoftskillsCourseId);
        updateCourseMenu();
    }
    
    /**
     * Show alert when no course is available for the selected type
     */
    private void showNoCourseAlert(String courseType) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No " + courseType + " Course");
        alert.setHeaderText(courseType + " Course Not Found");
        alert.setContentText("You don't have a " + courseType.toLowerCase() + " course yet.\n\n" +
                           "Create a new interview prep course to get started!");
        alert.showAndWait();
    }
    
    /**
     * Switch to a specific course and reload dashboard content
     */
    private void switchToCourse(int courseId) {
        try {
            // Load the course
            GeneratedCourse course = courseProgressDAO.getCourseById(courseId);
            if (course != null) {
                activeCourse = course;
                SessionContext.setActiveCourseId(courseId);
                // Persist last-used course (chapter unknown here)
                try {
                    if (currentUser != null) {
                        new com.interviewai.dao.UserDAO().updateLastUsed(currentUser.getId(), courseId, SessionContext.getActiveChapterId());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to persist last-used course: " + e.getMessage());
                }
                
                // Reload dashboard with new course
                loadCourseProgress();
                
                System.out.println("✓ Switched to course: " + course.getCourseTitle());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to Load Course");
                alert.setContentText("The selected course could not be loaded from the database.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Error switching course: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onSwitchCourse() {
        System.out.println("Switch course dialog opened");
        
        // Get all interview prep records for the user
        List<InterviewPrepDAO.InterviewPrepRecord> records = 
            interviewPrepDAO.getAllInterviewPreps(currentUser.getId());
        
        if (records.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Courses");
            alert.setHeaderText("No Interview Courses Found");
            alert.setContentText("You don't have any interview prep courses yet.\n\n" +
                               "Create your first course to get started!");
            alert.showAndWait();
            return;
        }
        
        // Create a modern dialog to show course pairs
        showCourseSelectorDialog(records);
    }
    
    /**
     * Show a professional dialog to select from available course pairs
     */
    private void showCourseSelectorDialog(List<InterviewPrepDAO.InterviewPrepRecord> records) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Switch Interview Course");
        dialog.setHeaderText("Select Your Interview Prep Course");
        
        // Create custom content
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");
        
        Label instruction = new Label("Choose a course pair to switch to:");
        instruction.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        content.getChildren().add(instruction);
        
        // Create a button for each course pair
        VBox courseList = new VBox(10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        
        for (InterviewPrepDAO.InterviewPrepRecord record : records) {
            VBox courseCard = createCourseCard(record, dateFormat);
            courseList.getChildren().add(courseCard);
        }
        
        ScrollPane scrollPane = new ScrollPane(courseList);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        content.getChildren().add(scrollPane);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    /**
     * Create a professional course card for the selector dialog
     */
    private VBox createCourseCard(InterviewPrepDAO.InterviewPrepRecord record, SimpleDateFormat dateFormat) {
        VBox card = new VBox(8);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, rgba(31, 34, 40, 0.95), rgba(26, 29, 35, 0.9));" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(59, 130, 246, 0.3);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12px;" +
            "-fx-padding: 15;" +
            "-fx-cursor: hand;"
        );
        
        // Technical course
        if (record.technicalCourseId > 0) {
            Label techLabel = new Label("🎯 Technical: " + 
                (record.technicalTitle != null ? record.technicalTitle : "Untitled Course"));
            techLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            card.getChildren().add(techLabel);
        }
        
        // Soft skills course
        if (record.softskillsCourseId > 0) {
            Label softLabel = new Label("💬 Soft Skills: " + 
                (record.softskillsTitle != null ? record.softskillsTitle : "Untitled Course"));
            softLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            card.getChildren().add(softLabel);
        }
        
        // Date
        String dateStr = record.createdAt != null ? dateFormat.format(record.createdAt) : "Unknown";
        Label dateLabel = new Label("Created: " + dateStr);
        dateLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
        card.getChildren().add(dateLabel);
        
        // Buttons
        HBox buttonBox = new HBox(8);
        Button techBtn = new Button("View Technical");
        techBtn.setStyle(
            "-fx-background-color: rgba(59, 130, 246, 0.25);" +
            "-fx-text-fill: #93C5FD;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;"
        );
        techBtn.setOnAction(e -> {
            currentTechnicalCourseId = record.technicalCourseId;
            currentSoftskillsCourseId = record.softskillsCourseId;
            isViewingTechnical = true;
            switchToCourse(record.technicalCourseId);
            updateCourseMenu();
            ((Stage) techBtn.getScene().getWindow()).close();
        });
        
        Button softBtn = new Button("View Soft Skills");
        softBtn.setStyle(
            "-fx-background-color: rgba(34, 197, 94, 0.25);" +
            "-fx-text-fill: #86EFAC;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;"
        );
        softBtn.setOnAction(e -> {
            currentTechnicalCourseId = record.technicalCourseId;
            currentSoftskillsCourseId = record.softskillsCourseId;
            isViewingTechnical = false;
            switchToCourse(record.softskillsCourseId);
            updateCourseMenu();
            ((Stage) softBtn.getScene().getWindow()).close();
        });
        
        buttonBox.getChildren().addAll(techBtn, softBtn);
        card.getChildren().add(buttonBox);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            card.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 10, 0, 0, 0);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            card.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 10, 0, 0, 0);", "")
        ));
        
        return card;
    }
    
}