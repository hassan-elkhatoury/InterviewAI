package com.interviewai.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

import com.interviewai.dao.ProgressDAO;
import com.interviewai.dao.CourseProgressDAO;
import com.interviewai.model.User;
import com.interviewai.model.GeneratedCourse;
import com.interviewai.model.Chapter;
import com.interviewai.util.SessionContext;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

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
    @FXML private GridPane courseProgressGrid;
    @FXML private Label course1ProgressLabel;
    @FXML private ProgressBar course1ProgressBar;
    @FXML private Label course2ProgressLabel;
    @FXML private ProgressBar course2ProgressBar;
    @FXML private Label course3ProgressLabel;
    @FXML private ProgressBar course3ProgressBar;

    // FXML Components - Bottom Bar
    @FXML private Button backToDashboardBtn;
    @FXML private Label motivationLabel;

    @FXML private Label  streakCount ;
    @FXML private HBox streakDaysContainer;


    User user;

    ProgressDAO progressDAO;
    CourseProgressDAO courseProgressDAO;
        
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressDAO = new ProgressDAO();
        courseProgressDAO = new CourseProgressDAO();
        user = SessionContext.getCurrentUser();

        try {
            setupEventHandlers();
            loadPlaceholderData();
            setupCharts();
            activateProgressSidebarButton();
            loadCourseProgress();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately, e.g., show an error message or log it
        }
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
    private void setupCharts() throws SQLException {
        setupXPChart();
        setupStreakChart();
    }

    /**
     * Setup XP Line Chart with placeholder data
     */
    private void setupXPChart() throws SQLException {
        if (xpChart == null) return;


        ProgressDAO progress = new ProgressDAO();
        Map<String, Integer> last7DaysXp = progress.getLast7DaysXp(user.getId());

        XYChart.Series<String, Number> xpSeries = new XYChart.Series<>();
        xpSeries.setName("Daily XP");
        xpChart.getData().clear();

        if (last7DaysXp != null && !last7DaysXp.isEmpty()) {

            for (Map.Entry<String, Integer> entry : last7DaysXp.entrySet()) {

                String day = entry.getKey();
                Integer xp = entry.getValue();

                xpSeries.getData().add(new XYChart.Data<>(day, xp));
            }
        }
       
       
        // Add series to chart
        xpChart.getData().add(xpSeries);
        
        // Styling
        xpChart.setLegendVisible(false);
        xpChart.setAnimated(false);
    }

    /**
     * Setup Streak Bar Chart with placeholder data
     */
    private void setupStreakChart() throws  SQLException {

            int streak = progressDAO.calculateUserStreak(user.getId());
            Map<String, Boolean> Last7DaysProgress = progressDAO.getLast7DaysProgress(user.getId());
            streakLabel.setText(String.valueOf(streak));
            streakCount.setText(String.valueOf(streak));

            
            streakDaysContainer.getChildren().clear();
            
        if (Last7DaysProgress != null && !Last7DaysProgress.isEmpty()) {

            for (Map.Entry<String, Boolean> entry : Last7DaysProgress.entrySet()){

                VBox dayStreak = new VBox();
                dayStreak.setAlignment(Pos.CENTER);
                dayStreak.setPrefHeight(150.0);
                dayStreak.setPrefWidth(200.0);

                Label dayLabel = new Label();
                dayLabel.setText(entry.getKey());

                dayStreak.getChildren().add(dayLabel);

                StackPane circleContainer = new StackPane();
                circleContainer.setPrefHeight(150.0);
                circleContainer.setPrefWidth(200.0);

                Circle checked = new Circle();
                checked.setRadius(18.0);

                Label checkedIcon = new Label();

                if(entry.getValue()){
                checked.getStyleClass().add("streak-checked");
                checkedIcon.setText("✔");
                
                checked.getStyleClass().add("checked-icon");
                checkedIcon.setStyle("-fx-text-fill: #043b18;");


                }else{

                    checked.getStyleClass().add("streak-unchecked");
                
                }


                circleContainer.getChildren().add(checked);
                circleContainer.getChildren().add(checkedIcon);

                dayStreak.getChildren().add(circleContainer);
                
                streakDaysContainer.getChildren().add(dayStreak);

            }



        }

       
        
    }

    /**
     * Load course progress cards dynamically from database for active user
     * Fetches real courses and chapters with completion data from the database
     */
    private void loadCourseProgress(){
        courseProgressGrid.getChildren().clear();

        if (user == null) {
            System.err.println("ERROR: No active user set in SessionContext");
            return;
        }

        try {
            // Fetch all ACTIVE courses for the current user from database
            java.util.List<java.util.Map<String, Object>> userCourses = progressDAO.getUserCourses(user.getId());
            
            if (userCourses == null || userCourses.isEmpty()) {
                System.out.println("INFO: No active courses found for user " + user.getId());
                return;
            }

            // Limit to 3 courses max for the grid layout
            int courseIndex = 0;
            for (java.util.Map<String, Object> courseMap : userCourses) {
                if (courseIndex >= 3) break;

                int courseId = (int) courseMap.get("course_id");
                String courseTitle = (String) courseMap.get("course_title");
                int progressPercentage = (int) courseMap.get("progress_percentage");

                // Fetch full course data with chapters from database
                GeneratedCourse course = courseProgressDAO.getCourseById(courseId);
                if (course == null) {
                    System.err.println("WARNING: Could not load course data for courseId " + courseId);
                    continue;
                }

                // Create main course card
                VBox courseCard = new VBox();
                courseCard.setAlignment(Pos.TOP_LEFT);
                courseCard.getStyleClass().add("course-card");
                courseCard.setSpacing(14.0);

                // Create course header
                HBox cardHeader = new HBox();
                cardHeader.getStyleClass().add("course-header");
                cardHeader.setAlignment(Pos.CENTER_LEFT);
                cardHeader.setSpacing(12.0);

                // Course icon - simple emoji based on course type
                String courseIcon = getCourseIcon(courseTitle);
                Label courseIconLabel = new Label(courseIcon);
                courseIconLabel.getStyleClass().add("course-icon");

                // Course info (name and stats)
                VBox courseInfo = new VBox();
                courseInfo.setSpacing(4.0);

                Label courseName = new Label(courseTitle);
                courseName.getStyleClass().add("course-name");

                // Build stats: chapters and questions count
                java.util.List<Chapter> chapters = course.getChapters();
                int totalChapters = chapters != null ? chapters.size() : 0;
                int completedChapters = 0;
                int totalQuestions = 0;

                if (chapters != null) {
                    completedChapters = (int) chapters.stream()
                        .filter(ch -> ch.getStatus() == Chapter.ChapterStatus.COMPLETED)
                        .count();
                    totalQuestions = chapters.stream()
                        .mapToInt(Chapter::getTotalQuestions)
                        .sum();
                }

                String statsText = completedChapters + " / " + totalChapters + " chapters • " + totalQuestions + " questions";
                Label questionsInfo = new Label(statsText);
                questionsInfo.getStyleClass().add("course-stats");

                courseInfo.getChildren().addAll(courseName, questionsInfo);
                HBox.setHgrow(courseInfo, javafx.scene.layout.Priority.ALWAYS);

                // Course percentage
                Label coursePercentage = new Label(progressPercentage + "%");
                coursePercentage.getStyleClass().add("course-percentage");

                cardHeader.getChildren().addAll(courseIconLabel, courseInfo, coursePercentage);

                // Course progress bar
                ProgressBar courseProgressBar = new ProgressBar();
                double progressValue = progressPercentage / 100.0;
                courseProgressBar.setProgress(progressValue);
                courseProgressBar.setPrefHeight(8.0);
                courseProgressBar.getStyleClass().add("course-progress-bar");

                // Course body - scrollable chapters list (max 4 chapters visible)
                VBox chaptersContent = new VBox();
                chaptersContent.setAlignment(Pos.TOP_LEFT);
                chaptersContent.setSpacing(8.0);
                chaptersContent.getStyleClass().add("chapters-list");

                // Add chapter items from database (limited to visible chapters, rest scroll)
                int chapterCount = 0;
                if (chapters != null) {
                    for (Chapter chapter : chapters) {
                        HBox chapterCard = new HBox();
                        chapterCard.setAlignment(Pos.CENTER_LEFT);
                        chapterCard.setSpacing(10.0);
                        chapterCard.getStyleClass().add("chapter-item");

                        // Chapter status icon and class based on chapter status
                        String statusIcon = getStatusIcon(chapter.getStatus());
                        String statusClass = getStatusClass(chapter.getStatus());
                        Label statusIconLabel = new Label(statusIcon);
                        statusIconLabel.getStyleClass().add("chapter-status-icon");
                        statusIconLabel.getStyleClass().add(statusClass);

                        // Chapter info
                        VBox chapterInfo = new VBox();
                        chapterInfo.setSpacing(2.0);
                        HBox.setHgrow(chapterInfo, javafx.scene.layout.Priority.ALWAYS);

                        Label chapterName = new Label(chapter.getName());
                        chapterName.getStyleClass().add("chapter-name");
                        if (statusClass.equals("locked")) {
                            chapterName.getStyleClass().add("locked");
                        }

                        // Progress text: "X / Y questions • ZZ%"
                        int completed = chapter.getCompletedQuestions();
                        int total = chapter.getTotalQuestions();
                        int percent = total > 0 ? (int)(100.0 * completed / total) : 0;
                        String progressText = completed + " / " + total + " questions • " + percent + "%";
                        
                        Label chapterProgress = new Label(progressText);
                        chapterProgress.getStyleClass().add("chapter-progress-text");

                        chapterInfo.getChildren().addAll(chapterName, chapterProgress);

                        // Chapter progress bar
                        ProgressBar chapterProgressBar = new ProgressBar();
                        chapterProgressBar.setPrefHeight(4.0);
                        double chapterProgress_value = total > 0 ? (double) completed / total : 0.0;
                        chapterProgressBar.setProgress(chapterProgress_value);
                        chapterProgressBar.getStyleClass().add("chapter-mini-progress");

                        chapterCard.getChildren().addAll(statusIconLabel, chapterInfo, chapterProgressBar);
                        chaptersContent.getChildren().add(chapterCard);
                        chapterCount++;
                    }
                }

                // Wrap chapters in a ScrollPane if more than 4 chapters
                javafx.scene.layout.VBox courseBody = new javafx.scene.layout.VBox();
                courseBody.setStyle("-fx-padding: 0;");
                
                if (chapterCount > 4) {
                    ScrollPane scrollPane = new ScrollPane(chaptersContent);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setPrefHeight(280); // Show ~4 chapters at a time
                    scrollPane.setStyle("-fx-control-inner-background: transparent; -fx-padding: 0;");
                    scrollPane.getStyleClass().add("chapters-scroll");
                    
                    // Minimal scrollbar styling
                    scrollPane.setStyle(
                        "-fx-control-inner-background: transparent; " +
                        "-fx-padding: 0; " +
                        "-fx-font-size: 12;"
                    );
                    
                    courseBody.getChildren().add(scrollPane);
                    VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
                } else {
                    // No scroll needed, add directly
                    courseBody.getChildren().add(chaptersContent);
                }

                // Assemble course card
                courseCard.getChildren().addAll(cardHeader, courseProgressBar, courseBody);

                // Add course card to grid with proper positioning
                // First course: column 0, row 0
                // Second course: column 1, row 0
                // Third course: column 0-1 span, row 1
                if (courseIndex == 0) {
                    GridPane.setColumnIndex(courseCard, 0);
                    GridPane.setRowIndex(courseCard, 0);
                } else if (courseIndex == 1) {
                    GridPane.setColumnIndex(courseCard, 1);
                    GridPane.setRowIndex(courseCard, 0);
                } else if (courseIndex == 2) {
                    GridPane.setColumnIndex(courseCard, 0);
                    GridPane.setRowIndex(courseCard, 1);
                    GridPane.setColumnSpan(courseCard, 2); // Span both columns
                }

                courseProgressGrid.getChildren().add(courseCard);
                courseIndex++;
            }

            System.out.println("✓ Loaded " + courseIndex + " course(s) for user " + user.getId());

        } catch (java.sql.SQLException e) {
            System.err.println("ERROR loading course progress: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get emoji icon based on course title
     */
    private String getCourseIcon(String courseTitle) {
        if (courseTitle == null) return "📚";
        String lower = courseTitle.toLowerCase();
        if (lower.contains("java")) return "☕";
        if (lower.contains("data") || lower.contains("struct")) return "🗂️";
        if (lower.contains("web") || lower.contains("html") || lower.contains("css")) return "🌐";
        if (lower.contains("python")) return "🐍";
        if (lower.contains("javascript")) return "⚛️";
        if (lower.contains("database") || lower.contains("sql")) return "🗄️";
        if (lower.contains("react")) return "⚛️";
        return "📚"; // Default
    }

    /**
     * Get status icon based on chapter status
     */
    private String getStatusIcon(Chapter.ChapterStatus status) {
        if (status == null) status = Chapter.ChapterStatus.NOT_STARTED;
        switch (status) {
            case COMPLETED:
                return "✓";
            case IN_PROGRESS:
                return "▶";
            case NOT_STARTED:
            default:
                return "🔒";
        }
    }

    /**
     * Get CSS status class based on chapter status
     */
    private String getStatusClass(Chapter.ChapterStatus status) {
        if (status == null) status = Chapter.ChapterStatus.NOT_STARTED;
        switch (status) {
            case COMPLETED:
                return "completed";
            case IN_PROGRESS:
                return "in-progress";
            case NOT_STARTED:
            default:
                return "locked";
        }
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
    public void refreshProgressData() throws SQLException {
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
