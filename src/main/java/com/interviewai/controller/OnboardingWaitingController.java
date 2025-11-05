package com.interviewai.controller;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Controller for the waiting/loading screen shown while AI generates the course.
 * Displays a spinning animation and loading text.
 */
public class OnboardingWaitingController {
    
    @FXML
    private StackPane loadingSpinner;
    
    @FXML
    public void initialize() {
        if (loadingSpinner != null) {
            // Create rotation animation for the spinner
            RotateTransition rotation = new RotateTransition(Duration.seconds(2), loadingSpinner);
            rotation.setFromAngle(0);
            rotation.setToAngle(360);
            rotation.setCycleCount(RotateTransition.INDEFINITE);
            rotation.play();
        }
    }
}
