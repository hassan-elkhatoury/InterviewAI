package com.interviewai.controller;

import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Handles the onboarding flow (role selection, goals, skill level).
 * TODO: Wire multi-step wizard; currently single-step continue → Dashboard.
 */
public class OnboardingController {

    @FXML
    private void onContinue(ActionEvent event) {
        // TODO: Validate onboarding selections and persist via OnboardingService
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneNavigator.switchTo(stage, Routes.DASHBOARD, 900, 600);
        } catch (Exception e) {
            // TODO: centralize error dialogs
        }
    }
}
