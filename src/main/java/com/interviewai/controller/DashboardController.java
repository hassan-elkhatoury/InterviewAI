package com.interviewai.controller;

import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Main app hub after login/onboarding.
 * Shows XP, streaks, notifications and quick actions to continue learning.
 */
public class DashboardController {

	@FXML
	private void onOpenProfile(ActionEvent event) {
		try {
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			SceneNavigator.switchTo(stage, Routes.PROFILE, 900, 600);
		} catch (Exception ignored) { }
	}

	@FXML
	private void onOpenCourses(ActionEvent event) {
		try {
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			SceneNavigator.switchTo(stage, Routes.COURSE_MAP, 900, 600);
		} catch (Exception ignored) { }
	}
}
