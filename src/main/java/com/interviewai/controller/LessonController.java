package com.interviewai.controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import com.interviewai.dao.CourseProgressDAO;
import com.interviewai.dao.ProgressDAO;
import com.interviewai.dao.QuestionDAO;
import com.interviewai.model.Chapter;
import com.interviewai.model.Question;
import com.interviewai.model.User;
import com.interviewai.service.OpenRouterAIService;
import com.interviewai.util.SessionContext;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the modern futuristic lesson/question view
 */
public class LessonController implements Initializable {

    // FXML Components
    @FXML private Label chapterTitleLabel;
    @FXML private Label questionProgressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressPercentLabel;
    @FXML private Label questionNumberBadge;
    @FXML private Label questionTypeLabel;
    @FXML private Label questionStatusLabel;
    @FXML private Label questionTextLabel;
    @FXML private VBox choicesContainer;
    @FXML private VBox explanationPanel;
    @FXML private Label explanationTextLabel;
    @FXML private Label correctAnswerLabel;
    @FXML private VBox shortAnswerBox;
    @FXML private TextArea shortAnswerField;
    @FXML private Button submitButton;
    @FXML private Button nextButton;
    @FXML private Button finishButton;
    @FXML private HBox feedbackBanner;
    @FXML private Label feedbackIcon;
    @FXML private Label feedbackTitle;
    @FXML private Label feedbackMessage;
    @FXML private javafx.scene.layout.StackPane toastOverlay;
    
    // AI Validation Panel Components
    @FXML private VBox aiValidationPanel;
    @FXML private VBox aiTipsBox;
    @FXML private VBox aiLoadingBox;
    @FXML private javafx.scene.control.ProgressIndicator aiLoadingSpinner;
    @FXML private VBox aiScoreBox;
    @FXML private Label aiScoreLabel;
    @FXML private ProgressBar aiScoreBar;
    @FXML private VBox aiStrengthsBox;
    @FXML private Label aiStrengthsLabel;
    @FXML private VBox aiImprovementsBox;
    @FXML private Label aiImprovementsLabel;
    @FXML private HBox aiToggleButtonsBox;
    @FXML private Button showExplanationButton;
    @FXML private Button showFeedbackButton;
    @FXML private VBox aiExplanationBox;
    @FXML private Label aiExplanationLabel;
    
    // Cache for explanation to avoid regeneration
    private String cachedExplanation = null;

    // Alert notification components (will be created dynamically)
    private VBox alertNotification;
    private VBox toastNotification;

    // Data
    private QuestionDAO questionDAO;
    private CourseProgressDAO courseProgressDAO;
    private ProgressDAO progressDAO;
    private OpenRouterAIService aiService;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private Question currentQuestion;
    private List<ButtonBase> choiceControls;
    private ToggleGroup toggleGroup;
    private String userAnswer;
    private static final int XP_PER_CORRECT_ANSWER = 10;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        questionDAO = new QuestionDAO();
        courseProgressDAO = new CourseProgressDAO();
        progressDAO = new ProgressDAO();
        
        // Initialize AI service
        try {
            aiService = new OpenRouterAIService();
        } catch (Exception e) {
            System.err.println("Warning: AI service not available: " + e.getMessage());
            aiService = null;
        }
        
        questions = new ArrayList<>();
        choiceControls = new ArrayList<>();

        // Record activity to update streak immediately when lesson is opened
        recordActivity();

        // Load questions for the active chapter
        loadChapterQuestions();
        
        if (!questions.isEmpty()) {
            // Find first unanswered question
            int firstUnansweredIndex = findFirstUnansweredQuestion();
            displayQuestion(firstUnansweredIndex);
        } else {
            showNoQuestionsMessage();
        }

        // Activate lesson button in sidebar - do this after a short delay to ensure sidebar is ready
        javafx.application.Platform.runLater(() -> {
            activateLessonSidebarButton();
        });
    }

    /**
     * Activate the lesson button in sidebar
     */
    private void activateLessonSidebarButton() {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null && mainLayout.getSidebarController() != null) {
            // This will update the sidebar to show lesson button as active
            mainLayout.getSidebarController().setActiveButton("lesson");
        }
    }

    /**
     * Find the first unanswered question index
     */
    private int findFirstUnansweredQuestion() {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if ((q.getStatus() != Question.QuestionStatus.COMPLETED) && (q.getStatus() != Question.QuestionStatus.INCORRECT)) {
                System.out.println("Found first unanswered question at index: " + i);
                return i;
            }
        }
        // All questions answered, return last question
        System.out.println("All questions answered, showing last question");
        return questions.size() - 1;
    }

    /**
     * Load all questions for the current chapter from database
     */
    private void loadChapterQuestions() {
        Integer chapterIdObj = SessionContext.getActiveChapterId();
        int chapterId = (chapterIdObj != null) ? chapterIdObj : 0;
        
        System.out.println("DEBUG: Active Chapter ID from SessionContext: " + chapterId);
        
        if (chapterId == 0) {
            System.err.println("ERROR: No active chapter set in SessionContext!");
            showAlert("No Chapter Selected", "Please select a chapter from the dashboard first.");
            return;
        }

        try {
            questions = questionDAO.getQuestionsByChapterId(chapterId);
            System.out.println("SUCCESS: Loaded " + questions.size() + " questions for chapter " + chapterId);
            
            // Shuffle questions to mix multiple-choice and short-answer types
            // Use chapter ID as seed for consistent shuffle order
            if (!questions.isEmpty()) {
                Random random = new Random(chapterId);
                Collections.shuffle(questions, random);
                System.out.println("✓ Shuffled " + questions.size() + " questions for chapter " + chapterId);
            }
            
            if (questions.isEmpty()) {
                System.out.println("WARNING: No questions found in database for chapter " + chapterId);
                showAlert("No Questions Available", 
                    "This chapter doesn't have any questions yet. Questions will be added soon.");
            }
        } catch (SQLException e) {
            System.err.println("DATABASE ERROR: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", 
                "Failed to load questions from database.\nError: " + e.getMessage() + 
                "\n\nPlease check:\n1. Database connection\n2. Chapter ID: " + chapterId);
        }
    }

    /**
     * Display a specific question by index
     */
    private void displayQuestion(int index) {
        if (index < 0 || index >= questions.size()) {
            return;
        }
        
        // Hide old explanation/feedback immediately
        if (explanationPanel != null) {
            explanationPanel.setVisible(false);
            explanationPanel.setManaged(false);
        }
        if (feedbackBanner != null) {
            feedbackBanner.setVisible(false);
            feedbackBanner.setManaged(false);
        }

        currentQuestionIndex = index;
        currentQuestion = questions.get(index);
        userAnswer = null;

        // Calculate progress-based question number (how many completed + 1)
        int completedCount = 0;
        for (Question q : questions) {
            if (q.getStatus() == Question.QuestionStatus.COMPLETED || 
                q.getStatus() == Question.QuestionStatus.INCORRECT) {
                completedCount++;
            }
        }
        int progressBasedNumber = completedCount + 1;

        // Update header
        chapterTitleLabel.setText("Chapter Questions");
        questionProgressLabel.setText("Question " + progressBasedNumber + " of " + questions.size());
        
        // Update progress bar based on completed count
        double progress = (double) completedCount / questions.size();
        progressBar.setProgress(progress);
        progressPercentLabel.setText(String.format("%.0f%% Complete", progress * 100));

        // Update question card
        questionNumberBadge.setText("Q" + progressBasedNumber);
        questionTypeLabel.setText(getQuestionTypeDisplay(currentQuestion.getQuestionType()));
        questionStatusLabel.setText("●");
        questionStatusLabel.setStyle("-fx-text-fill: #94a3b8;");
        questionTextLabel.setText(currentQuestion.getQuestion());

    // Build answer UI (this will handle submit button visibility)
    buildAnswerUI();
    
        // Show/hide AI validation panel based on question type
        if (aiValidationPanel != null) {
            if (currentQuestion.getQuestionType() == Question.QuestionType.SHORT_ANSWER ||
                currentQuestion.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE) {
                aiValidationPanel.setVisible(true);
                aiValidationPanel.setManaged(true);
                clearAIPanel();
                updateTipsText(currentQuestion.getQuestionType());
            } else {
                aiValidationPanel.setVisible(false);
                aiValidationPanel.setManaged(false);
            }
        }

        // Hide explanation and feedback
    explanationPanel.setVisible(false);
    explanationPanel.setManaged(false);
    feedbackBanner.setVisible(false);
    feedbackBanner.setManaged(false);

    // Hide next and finish buttons
    nextButton.setVisible(false);
    nextButton.setManaged(false);

    finishButton.setVisible(false);
    finishButton.setManaged(false);
    }

    /**
     * Build answer choices based on question type
     */
    private void buildAnswerUI() {
        choicesContainer.getChildren().clear();
        choiceControls.clear();
        toggleGroup = new ToggleGroup();
        
        // Short answer mode
        if (currentQuestion.getQuestionType() == Question.QuestionType.SHORT_ANSWER) {
            if (shortAnswerBox != null) {
                shortAnswerBox.setVisible(true);
                shortAnswerBox.setManaged(true);
                shortAnswerField.clear();
                
                // Remove any existing listeners to avoid duplicates
                shortAnswerField.textProperty().removeListener((observable, oldValue, newValue) -> {});
                
                // Add text listener to enable submit button when user types something
                shortAnswerField.textProperty().addListener((observable, oldValue, newValue) -> {
                    boolean hasText = newValue != null && !newValue.trim().isEmpty();
                    submitButton.setVisible(hasText);
                    submitButton.setManaged(hasText);
                });
                
                // Initially hide submit button for short answer
                submitButton.setVisible(false);
                submitButton.setManaged(false);
            shortAnswerField.setEditable(true); // Re-enable editing
            shortAnswerField.setStyle("");      // Clear any validation styles
            }
            // Hide choices
            choicesContainer.setVisible(false);
            choicesContainer.setManaged(false);
            return;
        }

        // Multiple choice mode (show choices, hide short answer)
        if (shortAnswerBox != null) {
            shortAnswerBox.setVisible(false);
            shortAnswerBox.setManaged(false);
        }
        choicesContainer.setVisible(true);
        choicesContainer.setManaged(true);

        // Show submit button for multiple choice questions
        submitButton.setVisible(true);
        submitButton.setManaged(true);

        List<String> choices = currentQuestion.getChoices();
        if (choices == null) choices = new ArrayList<>();
        boolean isMultipleChoice = currentQuestion.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE;
        for (int i = 0; i < choices.size(); i++) {
            String choiceText = choices.get(i);
            HBox choiceBox = createChoiceBox(choiceText, i, isMultipleChoice);
            choicesContainer.getChildren().add(choiceBox);
        }
    }

    /**
     * Create a single choice box
     */
    private HBox createChoiceBox(String choiceText, int index, boolean isMultipleChoice) {
        HBox choiceBox = new HBox(16);
        choiceBox.setAlignment(Pos.CENTER_LEFT);
        choiceBox.getStyleClass().add("choice-box");

        // Create radio button or checkbox
        ButtonBase control;
        if (isMultipleChoice) {
            RadioButton radio = new RadioButton();
            radio.setToggleGroup(toggleGroup);
            radio.getStyleClass().add("choice-radio");
            control = radio;
        } else {
            CheckBox checkBox = new CheckBox();
            checkBox.getStyleClass().add("choice-checkbox");
            control = checkBox;
        }

        // Store the choice text in user data
        control.setUserData(choiceText);
        choiceControls.add(control);  // Changed from choiceToggles

        // Choice label
        Label choiceLabel = new Label(choiceText);
        choiceLabel.getStyleClass().add("choice-label");
        choiceLabel.setWrapText(true);
        choiceLabel.setMaxWidth(Double.MAX_VALUE);

        // Status icon (hidden initially)
        Label statusIcon = new Label();
        statusIcon.getStyleClass().add("choice-icon");
        statusIcon.setVisible(false);

        choiceBox.getChildren().addAll(control, choiceLabel, statusIcon);

        // Click handler for the entire box - manage selection outline correctly
        choiceBox.setOnMouseClicked(event -> {
            if (control instanceof RadioButton) {
                // Clear selected style from all boxes first
                for (int i = 0; i < choicesContainer.getChildren().size(); i++) {
                    ((HBox) choicesContainer.getChildren().get(i)).getStyleClass().remove("choice-box-selected");
                }
                ((RadioButton) control).setSelected(true);
                choiceBox.getStyleClass().add("choice-box-selected");
            } else if (control instanceof CheckBox) {
                CheckBox cb = (CheckBox) control;
                cb.setSelected(!cb.isSelected());
                if (cb.isSelected()) {
                    if (!choiceBox.getStyleClass().contains("choice-box-selected")) {
                        choiceBox.getStyleClass().add("choice-box-selected");
                    }
                } else {
                    choiceBox.getStyleClass().remove("choice-box-selected");
                }
            }
        });

        return choiceBox;
    }

    /**
     * Handle submit answer button
     */
    @FXML
    private void onSubmitAnswer() {
        // Get user's selected answer
        userAnswer = getUserSelectedAnswer();

        if (userAnswer == null || userAnswer.isEmpty()) {
            showAlert("No Answer Selected", "Please select an answer before submitting.");
            return;
        }

        // --- NEW LOGIC FOR SHORT ANSWER (AI Grading) ---
        if (currentQuestion.getQuestionType() == Question.QuestionType.SHORT_ANSWER) {
            if (aiService == null) {
                showAlert("AI Service Error", "AI service is not available. Cannot grade short answer.");
                return;
            }
            
            // Disable inputs/submit immediately
            submitButton.setVisible(false);
            submitButton.setManaged(false);
            if (shortAnswerField != null) {
                shortAnswerField.setEditable(false);
            }
            
            // Trigger AI Validation (Grading happens in callback)
            validateWithAI();
            return;
        }

        // --- ORIGINAL LOGIC FOR MULTIPLE CHOICE ---

        // Check if answer is correct (Exact match)
        boolean isCorrect = checkAnswer(userAnswer);

        // Show feedback
        showFeedback(isCorrect);

        // Show explanation
        showExplanation();

        // Highlight correct/incorrect choices
        highlightChoices(isCorrect);

        // Update question status
        questionStatusLabel.setText(isCorrect ? "✓" : "✗");
        questionStatusLabel.setStyle(isCorrect ? 
            "-fx-text-fill: #10b981;" : 
            "-fx-text-fill: #ef4444;");

        // Update navigation
        submitButton.setVisible(false);
        submitButton.setManaged(false);
        
        if (currentQuestionIndex < questions.size() - 1) {
            nextButton.setVisible(true);
            nextButton.setManaged(true);
            nextButton.setDisable(false);
        } else {
            finishButton.setVisible(true);
            finishButton.setManaged(true);
        }

        // Update question status in database
        try {
            Question.QuestionStatus newStatus = isCorrect ? 
                Question.QuestionStatus.COMPLETED : Question.QuestionStatus.IN_PROGRESS;
            questionDAO.updateQuestionStatus(currentQuestion.getId(), newStatus);
            
            // Update the question object's status
            currentQuestion.setStatus(newStatus);
            
            // Award XP for correct answers
            if (isCorrect) {
                awardXP();
            }
            
            // For multiple choice, show explanation immediately (auto-load)
            if (currentQuestion.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE) {
                requestExplanation();
            }
            
            // Check if chapter is complete and mark it
            checkAndUpdateChapterCompletion();
            
        } catch (SQLException e) {
            System.err.println("Error updating question status: " + e.getMessage());
        }
    }

    /**
     * Award XP to the user for correct answer
     */
    private void awardXP() {
        User currentUser = SessionContext.getCurrentUser();
        Integer courseId = SessionContext.getActiveCourseId();
        if (currentUser == null || courseId == null) return;
        
        try {
            progressDAO.saveProgress(
                currentUser.getId(),
                courseId,
                XP_PER_CORRECT_ANSWER
            );
            
            // Show XP notification
            showXPNotification(XP_PER_CORRECT_ANSWER);
            
            System.out.println("✓ Awarded " + XP_PER_CORRECT_ANSWER + " XP to user " + currentUser.getId());
        } catch (SQLException e) {
            System.err.println("Error awarding XP: " + e.getMessage());
        }
    }

    /**
     * Show XP gained notification
     */
    private void showXPNotification(int xp) {
        // XP notification is now handled in toast notification
        System.out.println("✓ Awarded " + xp + " XP");
    }

    /**
     * Check if all questions in chapter are completed and update chapter status
     */
    private void checkAndUpdateChapterCompletion() {
        try {
            Integer chapterId = SessionContext.getActiveChapterId();
            if (chapterId == null) {
                System.out.println("⚠ Cannot check chapter completion: No active chapter ID");
                return;
            }
            
            System.out.println("🔍 Checking chapter " + chapterId + " completion status...");
            boolean allCompleted = courseProgressDAO.areAllQuestionsCompleted(chapterId);
            
            System.out.println("📊 All questions answered? " + allCompleted);
            
            if (allCompleted) {
                courseProgressDAO.updateChapterStatus(chapterId, Chapter.ChapterStatus.COMPLETED);
                System.out.println("✅ Chapter " + chapterId + " marked as COMPLETED");
            } else {
                // Mark chapter as IN_PROGRESS if not already
                courseProgressDAO.ensureChapterInProgress(chapterId);
                System.out.println("📝 Chapter " + chapterId + " marked as IN_PROGRESS");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking chapter completion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Removed auto-advance behavior per user request

    /**
     * Find next unanswered question after current index
     */
    private int findNextUnansweredQuestion() {
        for (int i = currentQuestionIndex + 1; i < questions.size(); i++) {
            if (questions.get(i).getStatus() != Question.QuestionStatus.COMPLETED) {
                return i;
            }
        }
        return -1; // No more unanswered questions
    }

    /**
     * Get the user's selected answer
     */
    private String getUserSelectedAnswer() {
        if (currentQuestion.getQuestionType() == Question.QuestionType.SHORT_ANSWER) {
            if (shortAnswerField != null) {
                String text = shortAnswerField.getText();
                return text == null ? null : text.trim();
            }
        }
        for (ButtonBase control : choiceControls) {  // Changed from Toggle
            boolean isSelected = false;
            if (control instanceof RadioButton) {
                isSelected = ((RadioButton) control).isSelected();
            } else if (control instanceof CheckBox) {
                isSelected = ((CheckBox) control).isSelected();
            }
            
            if (isSelected) {
                return (String) control.getUserData();
            }
        }
        return null;
    }

    /**
     * Check if the answer is correct
     */
    private boolean checkAnswer(String answer) {
        String correctAnswer = currentQuestion.getCorrectAnswer();
        if (correctAnswer == null) return false;
        
        // Simple comparison - could be enhanced for partial matching
        return answer.trim().equalsIgnoreCase(correctAnswer.trim()) ||
               answer.contains(correctAnswer) ||
               correctAnswer.contains(answer.substring(0, Math.min(answer.length(), 3)));
    }

    /**
     * Show feedback banner
     */
    private void showFeedback(boolean isCorrect) {
        // Hide inline feedback banner - we'll use toast instead
        feedbackBanner.setVisible(false);
        feedbackBanner.setManaged(false);
        
        // Show toast notification
        showToastNotification(isCorrect);
    }
    
    /**
     * Show floating toast notification for answer feedback
     */
    private void showToastNotification(boolean isCorrect) {
        // Clear any existing toast from overlay
        if (toastOverlay != null) {
            toastOverlay.getChildren().clear();
        }
        
        // Create toast container
        toastNotification = new VBox(6);
        toastNotification.setAlignment(Pos.CENTER_LEFT);
        toastNotification.setMaxWidth(280);
        toastNotification.setMinWidth(280);
        toastNotification.setMaxHeight(VBox.USE_PREF_SIZE);
        toastNotification.setPrefHeight(VBox.USE_COMPUTED_SIZE);
        toastNotification.setStyle(
            "-fx-background-color: " + (isCorrect ? 
                "linear-gradient(from 0% 0% to 100% 100%, rgba(16, 185, 129, 0.96) 0%, rgba(5, 150, 105, 0.96) 100%)" : 
                "linear-gradient(from 0% 0% to 100% 100%, rgba(239, 68, 68, 0.96) 0%, rgba(220, 38, 38, 0.96) 100%)") + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 14 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 15, 0, 0, 3);" +
            "-fx-border-color: " + (isCorrect ? "#34d399" : "#f87171") + ";" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 10;" +
            "-fx-max-height: 100;"
        );
        
        // Icon and title in HBox
        Label icon = new Label(isCorrect ? "✓" : "✗");
        icon.setStyle("-fx-font-size: 20px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        
        Label title = new Label(isCorrect ? "Correct!" : "Incorrect");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        HBox headerBox = new HBox(10, icon, title);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        // Message
        Label message = new Label(isCorrect ? 
            "+" + XP_PER_CORRECT_ANSWER + " XP earned! 🎉" : 
            "Check the explanation below");
        message.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255, 255, 255, 0.95); -fx-wrap-text: true;");
        message.setWrapText(true);
        message.setMaxWidth(250);
        
        toastNotification.getChildren().addAll(headerBox, message);
        
        // Add to overlay (doesn't affect layout)
        if (toastOverlay != null) {
            toastOverlay.getChildren().add(toastNotification);
            
            // Fade in animation
            toastNotification.setOpacity(0);
            javafx.animation.Timeline fadeIn = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(toastNotification.opacityProperty(), 0)),
                new javafx.animation.KeyFrame(Duration.millis(200), new javafx.animation.KeyValue(toastNotification.opacityProperty(), 1.0))
            );
            fadeIn.play();
            
            // Auto-hide after 3 seconds with fade out animation
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                javafx.animation.Timeline fadeOut = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(toastNotification.opacityProperty(), 1.0)),
                    new javafx.animation.KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(toastNotification.opacityProperty(), 0.0))
                );
                fadeOut.setOnFinished(e -> {
                    toastOverlay.getChildren().clear();
                });
                fadeOut.play();
            });
            pause.play();
        }
    }

    /**
     * Show explanation panel
     */
    private void showExplanation() {
        explanationPanel.setVisible(true);
        explanationPanel.setManaged(true);
        explanationTextLabel.setText(currentQuestion.getExplanation());
        correctAnswerLabel.setText(currentQuestion.getCorrectAnswer());
    }

    /**
     * Highlight correct and incorrect choices
     */
    private void highlightChoices(boolean userWasCorrect) {
        String correctAnswer = currentQuestion.getCorrectAnswer();

        for (int i = 0; i < choicesContainer.getChildren().size(); i++) {
            HBox choiceBox = (HBox) choicesContainer.getChildren().get(i);
            ButtonBase control = choiceControls.get(i);  // Changed from choiceToggles
            String choiceText = (String) control.getUserData();
            Label statusIcon = (Label) choiceBox.getChildren().get(2);

            boolean isCorrectChoice = choiceText.contains(correctAnswer) || correctAnswer.contains(choiceText.substring(0, Math.min(choiceText.length(), 3)));
            
            boolean isSelected = false;
            if (control instanceof RadioButton) {
                isSelected = ((RadioButton) control).isSelected();
            } else if (control instanceof CheckBox) {
                isSelected = ((CheckBox) control).isSelected();
            }

            if (isCorrectChoice) {
                choiceBox.getStyleClass().add("choice-box-correct");
                statusIcon.setText("✓");
                statusIcon.getStyleClass().add("choice-icon-correct");
                statusIcon.setVisible(true);
            } else if (isSelected) {
                choiceBox.getStyleClass().add("choice-box-incorrect");
                statusIcon.setText("✗");
                statusIcon.getStyleClass().add("choice-icon-incorrect");
                statusIcon.setVisible(true);
            }

            // Disable all choices after submission
            control.setDisable(true);
            choiceBox.setOnMouseClicked(null);
        }
    }

   

    /**
     * Navigate to next question
     */
    @FXML
    private void onNext() {
        if (currentQuestionIndex < questions.size() - 1) {

            // Check if the current question is not completed (e.g., incorrect or skipped)
            if(currentQuestion.getStatus() != Question.QuestionStatus.COMPLETED){

                try {
                    Question.QuestionStatus newStatus = Question.QuestionStatus.INCORRECT;
                    
                    questionDAO.updateQuestionStatus(currentQuestion.getId(), newStatus);
                    // Update the question object's status
                    currentQuestion.setStatus(newStatus);
                    
                
                 } catch (SQLException e) {
                    System.err.println("Error updating question status: " + e.getMessage());
                }

            }

            displayQuestion(currentQuestionIndex + 1);
        }
    }

    /**
     * Finish the chapter
     */
    @FXML
    private void onFinish() {
        // Mark current question as INCORRECT if still IN_PROGRESS
        if(currentQuestion.getStatus()==Question.QuestionStatus.IN_PROGRESS){
            try {
                Question.QuestionStatus newStatus = Question.QuestionStatus.INCORRECT;
                questionDAO.updateQuestionStatus(currentQuestion.getId(), newStatus);
                // Update the question object's status
                currentQuestion.setStatus(newStatus);
            } catch (SQLException e) {
                System.err.println("Error updating question status: " + e.getMessage());
            }
        }

        // Check if all questions are completed and update chapter status
        checkAndUpdateChapterCompletion();

        // Refresh dashboard progress before navigating back
        MainLayoutController mainLayout = MainLayoutController.getInstance();
            
        if (mainLayout != null) {
            
            mainLayout.loadContent("/fxml/DashboardContent.fxml", "dashboard");
            
            // Refresh dashboard chapter progress after a short delay to ensure view is loaded
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> {
                if (mainLayout.getDashboardController() != null) {
                    mainLayout.getDashboardController().refreshSelectedChapterProgress();
                }
            });
            pause.play();
        }
    }

    /**
     * Get display text for question type
     */
    private String getQuestionTypeDisplay(Question.QuestionType type) {
        switch (type) {
            case MULTIPLE_CHOICE:
                return "Multiple Choice";
            case SHORT_ANSWER:
                return "Short Answer";
            default:
                return "Question";
        }
    }

    /**
     * Show message when no questions are available
     */
    private void showNoQuestionsMessage() {
        questionTextLabel.setText("No questions available for this chapter yet.");
        choicesContainer.getChildren().clear();
        submitButton.setDisable(true);
    }

    /**
     * Show custom styled alert notification instead of default dialog
     */
    private void showCustomAlert(String title, String message, String type) {
        // Remove any existing alert
        if (alertNotification != null && choicesContainer.getParent() instanceof VBox) {
            VBox parent = (VBox) choicesContainer.getParent();
            parent.getChildren().remove(alertNotification);
        }

        // Create new alert notification
        alertNotification = new VBox(8);
        alertNotification.setStyle(
            "-fx-background-color: " + (type.equals("error") ? "rgba(239, 68, 68, 0.15)" : "rgba(251, 191, 36, 0.15)") + ";" +
            "-fx-border-color: " + (type.equals("error") ? "#ef4444" : "#fbbf24") + ";" +
            "-fx-border-width: 2 0 0 0;" +
            "-fx-padding: 14 20;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;"
        );

        // Icon
        Label icon = new Label(type.equals("error") ? "⚠" : "ℹ");
        icon.setStyle("-fx-font-size: 24px; -fx-text-fill: " + (type.equals("error") ? "#ef4444" : "#fbbf24") + ";");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + 
            (type.equals("error") ? "#ef4444" : "#fbbf24") + ";");

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cbd5e1; -fx-wrap-text: true;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        VBox textBox = new VBox(4, titleLabel, messageLabel);
        HBox contentBox = new HBox(12, icon, textBox);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        alertNotification.getChildren().add(contentBox);

        // Add to parent container
        if (choicesContainer.getParent() instanceof VBox) {
            VBox parent = (VBox) choicesContainer.getParent();
            int index = parent.getChildren().indexOf(choicesContainer);
            parent.getChildren().add(index, alertNotification);

            // Auto-hide after 4 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(4));
            pause.setOnFinished(event -> parent.getChildren().remove(alertNotification));
            pause.play();
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        showCustomAlert(title, message, "error");
    }

    /**
     * Record user activity
     */
    private void recordActivity() {
        // Placeholder for recording user activity
        System.out.println("Activity recorded for user.");
    }

    /**
     * Clear AI validation panel
     */
    private void clearAIPanel() {
        if (aiScoreLabel != null) aiScoreLabel.setText("--");
        if (aiScoreBar != null) aiScoreBar.setProgress(0);
        if (aiStrengthsLabel != null) aiStrengthsLabel.setText("");
        if (aiImprovementsLabel != null) aiImprovementsLabel.setText("");
        if (aiExplanationLabel != null) aiExplanationLabel.setText("");
        if (aiLoadingBox != null) {
            aiLoadingBox.setVisible(false);
            aiLoadingBox.setManaged(false);
        }
        if (aiScoreBox != null) {
            aiScoreBox.setVisible(false);
            aiScoreBox.setManaged(false);
        }
        if (aiStrengthsBox != null) {
            aiStrengthsBox.setVisible(false);
            aiStrengthsBox.setManaged(false);
        }
        if (aiImprovementsBox != null) {
            aiImprovementsBox.setVisible(false);
            aiImprovementsBox.setManaged(false);
        }
        if (aiExplanationBox != null) {
            aiExplanationBox.setVisible(false);
            aiExplanationBox.setManaged(false);
        }
        cachedExplanation = null;
    }

    /**
     * Update tips text based on question type
     */
    private void updateTipsText(Question.QuestionType type) {
        if (aiTipsBox != null) {
            aiTipsBox.getChildren().clear();
            Label tipLabel = new Label();
            tipLabel.setWrapText(true);
            tipLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            
            if (type == Question.QuestionType.SHORT_ANSWER) {
                tipLabel.setText("Tip: Be concise and specific. AI will grade your answer based on key concepts.");
            } else {
                tipLabel.setText("Tip: Select the best answer. AI can explain why it's correct.");
            }
            aiTipsBox.getChildren().add(tipLabel);
        }
    }

    /**
     * Validate answer with AI
     */
    private void validateWithAI() {
        if (aiService == null) return;
        
        String answer = getUserSelectedAnswer();
        if (answer == null || answer.isEmpty()) return;
        
        // Show loading
        if (aiLoadingBox != null) {
            aiLoadingBox.setVisible(true);
            aiLoadingBox.setManaged(true);
        }
        
        // Run in background
        new Thread(() -> {
            try {
                OpenRouterAIService.ValidationResult result = aiService.validateAnswer(
                    currentQuestion.getQuestion(),
                    currentQuestion.getCorrectAnswer(),
                    answer
                );
                
                javafx.application.Platform.runLater(() -> {
                    // Hide loading
                    if (aiLoadingBox != null) {
                        aiLoadingBox.setVisible(false);
                        aiLoadingBox.setManaged(false);
                    }
                    
                    // Show results
                    if (aiScoreBox != null) {
                        aiScoreBox.setVisible(true);
                        aiScoreBox.setManaged(true);
                        aiScoreLabel.setText(result.getScore() + "/100");
                        aiScoreBar.setProgress(result.getScore() / 100.0);
                    }
                    
                    if (aiStrengthsBox != null) {
                        aiStrengthsBox.setVisible(true);
                        aiStrengthsBox.setManaged(true);
                        aiStrengthsLabel.setText(result.getStrengths());
                    }
                    
                    if (aiImprovementsBox != null) {
                        aiImprovementsBox.setVisible(true);
                        aiImprovementsBox.setManaged(true);
                        aiImprovementsLabel.setText(result.getImprovements());
                    }
                    
                    // Show toggle buttons
                    if (aiToggleButtonsBox != null) {
                        aiToggleButtonsBox.setVisible(true);
                        aiToggleButtonsBox.setManaged(true);
                        if (showExplanationButton != null) {
                            showExplanationButton.setVisible(true);
                            showExplanationButton.setManaged(true);
                        }
                        if (showFeedbackButton != null) {
                            showFeedbackButton.setVisible(false);
                            showFeedbackButton.setManaged(false);
                        }
                    }
                    
                    // Handle completion based on score
                    boolean isPass = result.getScore() >= 70;
                    showFeedback(isPass);
                    
                    if (isPass) {
                        awardXP();
                        try {
                            questionDAO.updateQuestionStatus(currentQuestion.getId(), Question.QuestionStatus.COMPLETED);
                            currentQuestion.setStatus(Question.QuestionStatus.COMPLETED);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    // Enable next button
                    if (currentQuestionIndex < questions.size() - 1) {
                        nextButton.setVisible(true);
                        nextButton.setManaged(true);
                        nextButton.setDisable(false);
                    } else {
                        finishButton.setVisible(true);
                        finishButton.setManaged(true);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    if (aiLoadingBox != null) {
                        aiLoadingBox.setVisible(false);
                        aiLoadingBox.setManaged(false);
                    }
                    showAlert("AI Error", "Failed to validate answer: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Show explanation view
     */
    @FXML
    private void onShowExplanation() {
        if (aiScoreBox != null) {
            aiScoreBox.setVisible(false);
            aiScoreBox.setManaged(false);
        }
        if (aiStrengthsBox != null) {
            aiStrengthsBox.setVisible(false);
            aiStrengthsBox.setManaged(false);
        }
        if (aiImprovementsBox != null) {
            aiImprovementsBox.setVisible(false);
            aiImprovementsBox.setManaged(false);
        }
        
        if (showExplanationButton != null) {
            showExplanationButton.setVisible(false);
            showExplanationButton.setManaged(false);
        }
        if (showFeedbackButton != null) {
            showFeedbackButton.setVisible(true);
            showFeedbackButton.setManaged(true);
        }
        
        requestExplanation();
    }

    /**
     * Show feedback view
     */
    @FXML
    private void onShowFeedback() {
        if (aiExplanationBox != null) {
            aiExplanationBox.setVisible(false);
            aiExplanationBox.setManaged(false);
        }
        
        if (aiScoreBox != null) {
            aiScoreBox.setVisible(true);
            aiScoreBox.setManaged(true);
        }
        if (aiStrengthsBox != null) {
            aiStrengthsBox.setVisible(true);
            aiStrengthsBox.setManaged(true);
        }
        if (aiImprovementsBox != null) {
            aiImprovementsBox.setVisible(true);
            aiImprovementsBox.setManaged(true);
        }
        
        if (showExplanationButton != null) {
            showExplanationButton.setVisible(true);
            showExplanationButton.setManaged(true);
        }
        if (showFeedbackButton != null) {
            showFeedbackButton.setVisible(false);
            showFeedbackButton.setManaged(false);
        }
    }

    /**
     * Request explanation from AI
     */
    private void requestExplanation() {
        if (aiService == null) return;
        
        // If we already have a cached explanation, show it
        if (cachedExplanation != null) {
            if (aiExplanationBox != null) {
                aiExplanationBox.setVisible(true);
                aiExplanationBox.setManaged(true);
                aiExplanationLabel.setText(cachedExplanation);
            }
            return;
        }
        
        // Show loading state in explanation box
        if (aiExplanationBox != null) {
            aiExplanationBox.setVisible(true);
            aiExplanationBox.setManaged(true);
            aiExplanationLabel.setText("Generating explanation...");
        }
        
        new Thread(() -> {
            String explanation = aiService.explainQuestion(
                currentQuestion.getQuestion(),
                currentQuestion.getCorrectAnswer(),
                userAnswer != null ? userAnswer : ""
            );
            
            cachedExplanation = explanation;
            
            javafx.application.Platform.runLater(() -> {
                if (aiExplanationLabel != null) {
                    aiExplanationLabel.setText(explanation);
                }
            });
        }).start();
    }
}

