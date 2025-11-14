package com.interviewai.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: Load incorrect questions from database
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
     * TODO: Implement database integration
     */
    private void loadIncorrectQuestions() {
        // Placeholder - will be implemented with real data
        System.out.println("Loading incorrect questions...");
        
        // Update stats
        if (incorrectCountLabel != null) {
            incorrectCountLabel.setText("12");
        }
        if (progressLabel != null) {
            progressLabel.setText("Showing 3 of 12 questions");
        }

        // Check if there are questions to display
        // If no questions, show empty state
        // emptyStateBox.setVisible(true);
        // emptyStateBox.setManaged(true);
    }

    /**
     * Filter to show all incorrect questions
     */
    private void onFilterAll() {
        System.out.println("Filter: All questions");
        
        // Update button styles
        filterAllBtn.getStyleClass().add("filter-btn-active");
        filterChapterBtn.getStyleClass().remove("filter-btn-active");
        
        // TODO: Load all incorrect questions
        loadIncorrectQuestions();
    }

    /**
     * Filter to show incorrect questions from current chapter only
     */
    private void onFilterByChapter() {
        System.out.println("Filter: Current chapter");
        
        // Update button styles
        filterChapterBtn.getStyleClass().add("filter-btn-active");
        filterAllBtn.getStyleClass().remove("filter-btn-active");
        
        // TODO: Load chapter-specific questions
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
