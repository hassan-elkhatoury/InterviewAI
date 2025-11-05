
package com.interviewai.controller;

import java.io.File;

import com.interviewai.model.OnboardingData;
import com.interviewai.model.User;
import com.interviewai.service.OnboardingService;
import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;
import com.interviewai.util.SessionContext;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Handles the multi-step onboarding flow with Duolingo-inspired UI.
 * Collects user preferences: interview type, language, timeline, and context.
 */
public class test {

    // Progress indicator
    @FXML private HBox progressContainer;
    
    // Step containers
    @FXML private VBox step1Container;
    @FXML private VBox step2Container;
    @FXML private VBox step3Container;
    @FXML private VBox step4Container;
    
    // Step 1: Interview Type Options
    @FXML private HBox optionJobInterview;
    @FXML private HBox optionVisaInterview;
    @FXML private HBox optionInternshipInterview;
    @FXML private HBox optionUniversityInterview;
    
    // Step 2: Language Options
    @FXML private HBox optionEnglish;
    @FXML private HBox optionFrench;
    @FXML private HBox optionArabic;
    @FXML private HBox optionSpanish;
    
    // Step 3: Timeline Options
    @FXML private HBox optionTomorrow;
    @FXML private HBox optionThisWeek;
    @FXML private HBox optionLater;
    
    // Step 4: Context
    @FXML private Label step4Question;
    @FXML private Label step4Subtitle;
    @FXML private TextField contextField;
    @FXML private VBox uploadArea;
    @FXML private Label uploadedFileName;
    
    // Navigation buttons
    @FXML private Button backButton;
    @FXML private Button nextButton;
    
    // State management
    private int currentStep = 1;
    private static final int TOTAL_STEPS = 4;
    
    private String selectedInterviewType;
    private String selectedLanguage;
    private String selectedTimeline;
    private String contextInfo;
    private File uploadedCV;
    
    private HBox selectedCard; // Track currently selected option card
    
    @FXML
    public void initialize() {
        updateProgressDots();
        updateNavigationButtons();
    }
    
    // ========== Step Navigation ==========
    
    @FXML
    private void onNext(ActionEvent event) {
        if (currentStep < TOTAL_STEPS) {
            currentStep++;
            showCurrentStep();
            updateProgressDots();
            updateNavigationButtons();
            
            // Update Step 4 question based on interview type
            if (currentStep == 4) {
                updateStep4Content();
            }
        } else {
            // Final step - complete onboarding
            completeOnboarding(event);
        }
    }
    
    @FXML
    private void onBack(ActionEvent event) {
        if (currentStep > 1) {
            currentStep--;
            showCurrentStep();
            updateProgressDots();
            updateNavigationButtons();
        }
    }
    
    private void showCurrentStep() {
        // Hide all steps
        step1Container.setVisible(false);
        step1Container.setManaged(false);
        step2Container.setVisible(false);
        step2Container.setManaged(false);
        step3Container.setVisible(false);
        step3Container.setManaged(false);
        step4Container.setVisible(false);
        step4Container.setManaged(false);
        
        // Show current step
        switch (currentStep) {
            case 1:
                step1Container.setVisible(true);
                step1Container.setManaged(true);
                break;
            case 2:
                step2Container.setVisible(true);
                step2Container.setManaged(true);
                break;
            case 3:
                step3Container.setVisible(true);
                step3Container.setManaged(true);
                break;
            case 4:
                step4Container.setVisible(true);
                step4Container.setManaged(true);
                break;
        }
    }
    
    private void updateProgressDots() {
        progressContainer.getChildren().clear();
        for (int i = 1; i <= TOTAL_STEPS; i++) {
            Region dot = new Region();
            dot.getStyleClass().add(i <= currentStep ? "progress-dot-active" : "progress-dot");
            progressContainer.getChildren().add(dot);
        }
    }
    
    private void updateNavigationButtons() {
        // Show back button after first step
        backButton.setVisible(currentStep > 1);
        
        // Enable next button only if current step has a selection
        boolean canProceed = false;
        switch (currentStep) {
            case 1:
                canProceed = selectedInterviewType != null;
                break;
            case 2:
                canProceed = selectedLanguage != null;
                break;
            case 3:
                canProceed = selectedTimeline != null;
                break;
            case 4:
                canProceed = contextField.getText() != null && !contextField.getText().isBlank();
                break;
        }
        nextButton.setDisable(!canProceed);
        
        // Change button text on last step
        nextButton.setText(currentStep == TOTAL_STEPS ? "Start Practicing" : "Next");
    }
    
    // ========== Step 1: Interview Type Selection ==========
    
    @FXML
    private void onSelectInterviewType(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        selectCard(source);
        
        if (source == optionJobInterview) {
            selectedInterviewType = "JOB";
        } else if (source == optionVisaInterview) {
            selectedInterviewType = "VISA";
        } else if (source == optionInternshipInterview) {
            selectedInterviewType = "INTERNSHIP";
        } else if (source == optionUniversityInterview) {
            selectedInterviewType = "UNIVERSITY";
        }
        
        updateNavigationButtons();
    }
    
    // ========== Step 2: Language Selection ==========
    
    @FXML
    private void onSelectLanguage(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        selectCard(source);
        
        if (source == optionEnglish) {
            selectedLanguage = "ENGLISH";
        } else if (source == optionFrench) {
            selectedLanguage = "FRENCH";
        } else if (source == optionArabic) {
            selectedLanguage = "ARABIC";
        } else if (source == optionSpanish) {
            selectedLanguage = "SPANISH";
        }
        
        updateNavigationButtons();
    }
    
    // ========== Step 3: Timeline Selection ==========
    
    @FXML
    private void onSelectTimeline(MouseEvent event) {
        HBox source = (HBox) event.getSource();
        selectCard(source);
        
        if (source == optionTomorrow) {
            selectedTimeline = "TOMORROW";
        } else if (source == optionThisWeek) {
            selectedTimeline = "THIS_WEEK";
        } else if (source == optionLater) {
            selectedTimeline = "LATER";
        }
        
        updateNavigationButtons();
    }
    
    // ========== Step 4: Context & CV Upload ==========
    
    private void updateStep4Content() {
        if ("JOB".equals(selectedInterviewType) || "INTERNSHIP".equals(selectedInterviewType)) {
            step4Question.setText("Which industry or position are you applying for?");
            contextField.setPromptText("e.g., Software Developer, Marketing Manager, Designer");
        } else if ("UNIVERSITY".equals(selectedInterviewType)) {
            step4Question.setText("Which program or major are you applying to?");
            contextField.setPromptText("e.g., Computer Science, Business Administration, Medicine");
        } else if ("VISA".equals(selectedInterviewType)) {
            step4Question.setText("What type of visa are you applying for?");
            contextField.setPromptText("e.g., Work Visa, Student Visa, Tourist Visa");
        }
        
        // Add listener to enable next button when text is entered
        contextField.textProperty().addListener((obs, old, newVal) -> updateNavigationButtons());
    }
    
    @FXML
    private void onUploadCV(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload CV/Resume");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        Stage stage = (Stage) uploadArea.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            uploadedCV = file;
            uploadedFileName.setText(file.getName());
            uploadedFileName.setVisible(true);
        }
    }
    
    // ========== UI Helpers ==========
    
    private void selectCard(HBox card) {
        // Remove selection from previously selected card
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("option-card-selected");
        }
        
        // Add selection to new card
        card.getStyleClass().add("option-card-selected");
        selectedCard = card;
    }
    
    // ========== Complete Onboarding ==========
    
    private void completeOnboarding(ActionEvent event) {
        contextInfo = contextField.getText();
        
        // Persist onboarding data via OnboardingService
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser != null) {
            OnboardingData data = new OnboardingData();
            data.setUserId(currentUser.getId());
            data.setInterviewType(selectedInterviewType);
            data.setLanguage(selectedLanguage);
            data.setTimeline(selectedTimeline);
            data.setContext(contextInfo);
            data.setCvPath(uploadedCV != null ? uploadedCV.getAbsolutePath() : null);
            
            OnboardingService service = new OnboardingService();
            service.saveOnboardingData(data);
            
            System.out.println("=== Onboarding Complete ===");
            System.out.println(data.toString());
        }
        
        try {
            Stage stage = (Stage) nextButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, Routes.DASHBOARD, stage.getWidth()-15, stage.getHeight()-38);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setContentText("Failed to navigate to dashboard: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
