package com.interviewai.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.interviewai.dao.CourseDAO;
import com.interviewai.dao.QuestionDAO;
import com.interviewai.model.Chapter;
import com.interviewai.model.Question;
import com.interviewai.util.SessionContext;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controller for the Review Incorrect Questions page
 * Displays all questions that were answered incorrectly for review
 */
public class ReviewController implements Initializable {

    // FXML Components - Header
    @FXML private Label reviewTitleLabel;
    @FXML private Label reviewSubtitleLabel;
    @FXML private Label incorrectCountLabel;

    // FXML Components - Filter Section
    @FXML private Button filterAllBtn;
    @FXML private Button filterChapterBtn;
    @FXML private Button retryAllBtn;

    // FXML Components - Content
    @FXML private VBox questionsContainer;
    @FXML private VBox emptyStateBox;

    // FXML Components - Bottom Bar
    @FXML private Button backToDashboardBtn;
    @FXML private Button startReviewBtn;
    @FXML private Label progressLabel;

    // Data
    private QuestionDAO questionDAO;
    private CourseDAO courseDAO;
    private List<Question> allIncorrectQuestions;
    private Map<Integer, String> chapterNames; // questionId -> chapterName
    private boolean filterByCurrentChapter = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        questionDAO = new QuestionDAO();
        courseDAO = new CourseDAO();
        chapterNames = new HashMap<>();
        allIncorrectQuestions = new ArrayList<>();
        
        setupEventHandlers();
        loadIncorrectQuestions();
        activateReviewSidebarButton();
    }
    
    


    /**
     * Activate the review button in sidebar
     */
    private void activateReviewSidebarButton() {
        javafx.application.Platform.runLater(() -> {
            MainLayoutController mainLayout = MainLayoutController.getInstance();
            if (mainLayout != null && mainLayout.getSidebarController() != null) {
                mainLayout.getSidebarController().setActiveButton("review");
            }
        });
    }

    /**
     * Setup event handlers for buttons
     */
    private void setupEventHandlers() {
        // Filter buttons
        if (filterAllBtn != null) {
            filterAllBtn.setOnAction(e -> onFilterAll());
        }
        if (filterChapterBtn != null) {
            filterChapterBtn.setOnAction(e -> onFilterByChapter());
        }

        // Retry all button
        if (retryAllBtn != null) {
            retryAllBtn.setOnAction(e -> onRetryAll());
        }

        // Back button
        if (backToDashboardBtn != null) {
            backToDashboardBtn.setOnAction(e -> onBackToDashboard());
        }
        
        // Start Review button
        if (startReviewBtn != null) {
            startReviewBtn.setOnAction(e -> onStartReview());
        }
    }

    /**
     * Load incorrect questions from database
     */
    private void loadIncorrectQuestions() {
        // Clear previous content
        questionsContainer.getChildren().clear();
        chapterNames.clear();
        
        // Get current course and chapter
        Integer courseId = SessionContext.getActiveCourseId();
        Integer currentChapterId = SessionContext.getActiveChapterId();
        
        if (courseId == null) {
            System.err.println("No active course selected");
            showEmptyState("No Course Selected", "Please select a course from the dashboard first.");
            return;
        }
        
        try {
            // Load incorrect questions based on filter
            if (filterByCurrentChapter && currentChapterId != null) {
                allIncorrectQuestions = questionDAO.getIncorrectQuestionsByChapterId(currentChapterId);
            } else {
                allIncorrectQuestions = questionDAO.getIncorrectQuestionsByCourseId(courseId);
            }
            
            // Update stats
            if (incorrectCountLabel != null) {
                incorrectCountLabel.setText(String.valueOf(allIncorrectQuestions.size()));
            }
            if (progressLabel != null) {
                progressLabel.setText("Showing " + allIncorrectQuestions.size() + " question(s)");
            }
            
            // Check if there are questions to display
            if (allIncorrectQuestions.isEmpty()) {
                showEmptyState("No Incorrect Questions", "Great job! You haven't answered any questions incorrectly yet.");
                return;
            }
            
            // Hide empty state
            if (emptyStateBox != null) {
                emptyStateBox.setVisible(false);
                emptyStateBox.setManaged(false);
            }
            
            // Load chapter names for questions
            loadChapterNamesForQuestions();
            
            // Display all questions
            for (int i = 0; i < allIncorrectQuestions.size(); i++) {
                Question question = allIncorrectQuestions.get(i);
                String chapterName = chapterNames.getOrDefault(question.getId(), "Unknown Chapter");
                createQuestionCard(question, i + 1, chapterName);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading incorrect questions: " + e.getMessage());
            e.printStackTrace();
            showEmptyState("Error Loading Questions", "Failed to load incorrect questions from database.");
        }
    }
    
    /**
     * Load chapter names for all questions
     */
    private void loadChapterNamesForQuestions() {
        try {
            // Query database to get chapter info for each question
            for (Question question : allIncorrectQuestions) {
                try {
                    Chapter chapter = getChapterForQuestion(question.getId());
                    if (chapter != null) {
                        chapterNames.put(question.getId(), chapter.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading chapter for question " + question.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading chapter names: " + e.getMessage());
        }
    }
    
    /**
     * Get chapter for a specific question
     */
    private Chapter getChapterForQuestion(int questionId) throws Exception {
        String sql = "SELECT c.id, c.chapter_number, c.name, c.description, c.status " +
                     "FROM chapters c " +
                     "INNER JOIN questions q ON c.id = q.chapter_id " +
                     "WHERE q.id = ?";
        
        try (java.sql.Connection conn = com.interviewai.dao.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, questionId);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Chapter chapter = new Chapter();
                    chapter.setId(rs.getInt("id"));
                    chapter.setChapterNumber(rs.getInt("chapter_number"));
                    chapter.setName(rs.getString("name"));
                    chapter.setDescription(rs.getString("description"));
                    chapter.setStatus(rs.getString("status"));
                    return chapter;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Show empty state message
     */
    private void showEmptyState(String title, String message) {
        questionsContainer.getChildren().clear();
        
        if (emptyStateBox != null) {
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);
        }
        
        // Update counts
        if (incorrectCountLabel != null) {
            incorrectCountLabel.setText("0");
        }
        if (progressLabel != null) {
            progressLabel.setText(message);
        }
    }
    
    /**
     * Create a question card UI element
     */
    private void createQuestionCard(Question question, int displayNumber, String chapterName) {
        VBox questionCard = new VBox();
        questionCard.getStyleClass().add("review-question-card");
        
        // Question Header
        HBox questionHeader = new HBox();
        questionHeader.setAlignment(Pos.CENTER_LEFT);
        questionHeader.setSpacing(12);
        questionHeader.getStyleClass().add("review-question-header");
        
        // Question number badge
        Label questionNumber = new Label();
        questionNumber.setText("Q" + displayNumber);
        questionNumber.getStyleClass().add("question-number-badge");
        
        // Chapter tag
        Label chapterTag = new Label();
        chapterTag.setText(chapterName);
        chapterTag.getStyleClass().add("chapter-tag");

        // Spacer
        Region divider = new Region();
        HBox.setHgrow(divider, Priority.ALWAYS);

        // Status badge
        Label questionStatus = new Label();
        questionStatus.setText("Incorrect");
        questionStatus.getStyleClass().add("status-badge-incorrect");

        // Assemble header
        questionHeader.getChildren().addAll(questionNumber, chapterTag, divider, questionStatus);

        // Question text
        Label questionText = new Label();
        questionText.setText(question.getQuestion());
        questionText.getStyleClass().add("review-question-text");
        questionText.setWrapText(true);

        // Assemble card
        questionCard.getChildren().addAll(questionHeader, questionText);
        
        // Add to container
        questionsContainer.getChildren().add(questionCard);
    }

    /**
     * Filter to show all incorrect questions
     */
    private void onFilterAll() {
        System.out.println("Filter: All questions");
        
        // Update filter state
        filterByCurrentChapter = false;
        
        // Update button styles
        filterAllBtn.getStyleClass().add("filter-btn-active");
        filterChapterBtn.getStyleClass().remove("filter-btn-active");
        
        // Reload questions
        loadIncorrectQuestions();
    }

    /**
     * Filter to show incorrect questions from current chapter only
     */
    private void onFilterByChapter() {
        System.out.println("Filter: Current chapter");
        
        Integer currentChapterId = SessionContext.getActiveChapterId();
        if (currentChapterId == null) {
            System.err.println("No active chapter selected");
            return;
        }
        
        // Update filter state
        filterByCurrentChapter = true;
        
        // Update button styles
        filterChapterBtn.getStyleClass().add("filter-btn-active");
        filterAllBtn.getStyleClass().remove("filter-btn-active");
        
        // Reload questions
        loadIncorrectQuestions();
    }

    /**
     * Retry all incorrect questions
     */
    private void onRetryAll() {
        System.out.println("Retry all questions");
        
        // TODO: Start a new lesson session with all incorrect questions
        // Navigate to lesson view with these questions
    }

    /**
     * Start reviewing incorrect questions
     */
    private void onStartReview() {
        System.out.println("Starting review session");
        
        // TODO: Navigate to lesson view with incorrect questions
        // For now, just show a message
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            // Will load ReviewLessonView in the future
            System.out.println("Review lesson page not yet implemented");
        }
    }

    /**
     * Navigate back to dashboard
     */
    private void onBackToDashboard() {
        System.out.println("Back to dashboard");
        
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.loadContent("/fxml/DashboardContent.fxml", "dashboard");
        }
    }

    /**
     * Handle "Mark as Learned" button for a question
     * TODO: Implement for dynamic question cards
     */
    private void onMarkAsLearned(int questionId) {
        System.out.println("Mark question " + questionId + " as learned");
        
        // TODO: Update database to mark question as reviewed/learned
        // Remove from incorrect list
        // Refresh display
    }

    /**
     * Handle "Try Again" button for a question
     * TODO: Implement for dynamic question cards
     */
    private void onTryAgain(int questionId) {
        System.out.println("Try again: question " + questionId);
        
        // TODO: Navigate to lesson view with this specific question
        // Update question attempt count
    }
}
