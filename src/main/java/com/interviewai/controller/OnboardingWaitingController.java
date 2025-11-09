package com.interviewai.controller;

import java.util.Arrays;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;

/**
 * Controller for the waiting/loading screen shown while AI generates the course.
 * Displays progress bar, percentage, and rotating motivational quotes.
 * Shows error message and retry button on failure.
 */
public class OnboardingWaitingController {
    
    @FXML
    private HBox progressFill;
    
    @FXML
    private Label progressPercentLabel;
    
    @FXML
    private Label motivationalQuoteLabel;
    
    @FXML
    private Label quoteAuthorLabel;
    
    @FXML
    private VBox errorContainer;
    
    @FXML
    private Label errorMessageLabel;
    
    @FXML
    private Button retryButton;
    
    private static OnboardingWaitingController instance;
    
    private Timeline quoteTimeline;
    private int currentQuoteIndex = 0;
    
    // 10 famous motivational quotes about interviews and success
    private final List<QuoteData> motivationalQuotes = Arrays.asList(
        new QuoteData("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill"),
        new QuoteData("The only way to do great work is to love what you do.", "Steve Jobs"),
        new QuoteData("Believe you can and you're halfway there.", "Theodore Roosevelt"),
        new QuoteData("Opportunities don't happen, you create them.", "Chris Grosser"),
        new QuoteData("Success usually comes to those who are too busy to be looking for it.", "Henry David Thoreau"),
        new QuoteData("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
        new QuoteData("The future depends on what you do today.", "Mahatma Gandhi"),
        new QuoteData("It's not whether you get knocked down, it's whether you get up.", "Vince Lombardi"),
        new QuoteData("Your limitation—it's only your imagination.", "Unknown"),
        new QuoteData("The way to get started is to quit talking and begin doing.", "Walt Disney")
    );
    
    @FXML
    public void initialize() {
        instance = this;
        
        // Initialize progress bar
        if (progressFill != null) {
            progressFill.setMaxWidth(0);
        }
        
        // Hide error container initially
        if (errorContainer != null) {
            errorContainer.setVisible(false);
            errorContainer.setManaged(false);
        }
        
        // Set up retry button action
        if (retryButton != null) {
            retryButton.setOnAction(event -> handleRetry());
        }
        
        // Start quote rotation (every 5 seconds)
        startQuoteRotation();
    }
    
    /**
     * Handle retry button click - navigate back to onboarding page.
     */
    @FXML
    private void handleRetry() {
        try {
            Stage stage = (Stage) retryButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, Routes.ONBOARDING, stage.getWidth()-15, stage.getHeight()-38);
        } catch (Exception e) {
            System.err.println("Failed to navigate back to onboarding: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show error message and retry button.
     */
    public void showError(String errorMessage) {
        Platform.runLater(() -> {
            if (errorMessageLabel != null) {
                errorMessageLabel.setText(errorMessage);
            }
            if (errorContainer != null) {
                errorContainer.setVisible(true);
                errorContainer.setManaged(true);
            }
            
            // Stop quote rotation
            if (quoteTimeline != null) {
                quoteTimeline.stop();
            }
        });
    }
    
    /**
     * Start rotating motivational quotes every 5 seconds.
     */
    private void startQuoteRotation() {
        quoteTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            currentQuoteIndex = (currentQuoteIndex + 1) % motivationalQuotes.size();
            updateQuote();
        }));
        quoteTimeline.setCycleCount(Timeline.INDEFINITE);
        quoteTimeline.play();
        
        // Set initial quote
        updateQuote();
    }
    
    /**
     * Update the displayed motivational quote.
     */
    private void updateQuote() {
        QuoteData quote = motivationalQuotes.get(currentQuoteIndex);
        if (motivationalQuoteLabel != null) {
            motivationalQuoteLabel.setText("\"" + quote.getText() + "\"");
        }
        if (quoteAuthorLabel != null) {
            quoteAuthorLabel.setText("— " + quote.getAuthor());
        }
    }
    
    /**
     * Update progress bar and percentage (called from OnboardingController).
     */
    public void updateProgress(String message, int percent) {
        Platform.runLater(() -> {
            // Update percentage label above progress bar
            if (progressPercentLabel != null) {
                progressPercentLabel.setText(percent + "%");
            }
            
            // Update progress bar fill (left to right animation)
            if (progressFill != null) {
                double maxWidth = 500.0; // Max width of progress bar container
                double newWidth = (percent / 100.0) * maxWidth;
                progressFill.setMaxWidth(newWidth);
                progressFill.setPrefWidth(newWidth);
            }
            
            System.out.println(String.format("Progress UI Updated: %d%% - %s", percent, message));
        });
    }
    
    /**
     * Get the singleton instance of this controller.
     */
    public static OnboardingWaitingController getInstance() {
        return instance;
    }
    
    /**
     * Clean up when leaving this page.
     */
    public void cleanup() {
        if (quoteTimeline != null) {
            quoteTimeline.stop();
        }
    }
    
    /**
     * Inner class to hold quote data.
     */
    private static class QuoteData {
        private final String text;
        private final String author;
        
        public QuoteData(String text, String author) {
            this.text = text;
            this.author = author;
        }
        
        public String getText() {
            return text;
        }
        
        public String getAuthor() {
            return author;
        }
    }
}
