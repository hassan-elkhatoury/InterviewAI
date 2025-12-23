package com.interviewai.controller;

import com.interviewai.dao.GoalDAO;
import com.interviewai.model.User;
import com.interviewai.model.UserGoal;
import com.interviewai.util.SessionContext;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Edit Goals Dialog
 */
public class EditGoalsDialogController implements Initializable {

    @FXML private VBox goalsListContainer;
    @FXML private TextField goalNameField;
    @FXML private ComboBox<String> goalTypeCombo;
    @FXML private TextField targetValueField;
    @FXML private Label targetUnitLabel;
    @FXML private Button addGoalButton;
    @FXML private Button headerCloseButton;
    @FXML private HBox dialogHeader;
    @FXML private Button cancelButton;

    private double xOffset = 0;
    private double yOffset = 0;

    private GoalDAO goalDAO;
    private User currentUser;
    private boolean changesMade = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        goalDAO = new GoalDAO();
        currentUser = SessionContext.getCurrentUser();

        setupGoalTypes();
        setupEventHandlers();
        setupWindowDragging();
        loadExistingGoals();
    }

    private void setupWindowDragging() {
        dialogHeader.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        dialogHeader.setOnMouseDragged(event -> {
            Stage stage = (Stage) dialogHeader.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    /**
     * Setup goal type combo box with available types
     */
    private void setupGoalTypes() {
        goalTypeCombo.getItems().addAll(
            "Answer Questions",
            "Earn XP",
            "Complete Chapters",
            "Study Time",
            "Enroll in Courses",
            "Maintain Streak"
        );

        // Update unit label when type changes
        goalTypeCombo.setOnAction(e -> updateUnitLabel());
    }

    /**
     * Update the unit label based on selected goal type
     */
    private void updateUnitLabel() {
        String selected = goalTypeCombo.getValue();
        if (selected == null) return;

        switch (selected) {
            case "Answer Questions":
                targetUnitLabel.setText("questions");
                break;
            case "Earn XP":
                targetUnitLabel.setText("XP points");
                break;
            case "Complete Chapters":
                targetUnitLabel.setText("chapters");
                break;
            case "Study Time":
                targetUnitLabel.setText("minutes");
                break;
            case "Enroll in Courses":
                targetUnitLabel.setText("courses");
                break;
            case "Maintain Streak":
                targetUnitLabel.setText("days");
                break;
        }
    }

    /**
     * Setup event handlers for buttons
     */
    private void setupEventHandlers() {
        addGoalButton.setOnAction(e -> onAddGoal());
        cancelButton.setOnAction(e -> onCancel());
        headerCloseButton.setOnAction(e -> closeDialog());
        
        // Hover effect for close button
        headerCloseButton.setOnMouseEntered(e -> headerCloseButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;"));
        headerCloseButton.setOnMouseExited(e -> headerCloseButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px; -fx-cursor: hand;"));
    }

    /**
     * Load existing goals from database
     */
    private void loadExistingGoals() {
        goalsListContainer.getChildren().clear();

        try {
            List<UserGoal> goals = goalDAO.getUserGoals(currentUser.getId());

            if (goals.isEmpty()) {
                Label emptyLabel = new Label("No goals yet. Add your first goal below!");
                emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-font-size: 13px; -fx-padding: 20 0; -fx-alignment: center;");
                emptyLabel.setMaxWidth(Double.MAX_VALUE);
                emptyLabel.setAlignment(Pos.CENTER);
                goalsListContainer.getChildren().add(emptyLabel);
                return;
            }

            for (UserGoal goal : goals) {
                HBox goalItem = createGoalItem(goal);
                goalsListContainer.getChildren().add(goalItem);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load goals: " + e.getMessage());
        }
    }

    /**
     * Create a visual item for a goal
     */
    private HBox createGoalItem(UserGoal goal) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("goal-list-item");

        // Goal info
        VBox info = new VBox(4);
        Label nameLabel = new Label(goal.getGoalName());
        nameLabel.getStyleClass().add("goal-item-title");

        String typeDisplay = getGoalTypeDisplay(goal.getGoalType());
        Label detailsLabel = new Label(typeDisplay + " • Target: " + goal.getTargetValue());
        detailsLabel.getStyleClass().add("goal-item-desc");

        info.getChildren().addAll(nameLabel, detailsLabel);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        // Delete button
        Button deleteBtn = new Button();
        deleteBtn.setText("✕");
        // deleteBtn.setGraphic(new javafx.scene.text.Text("🗑"));
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setTooltip(new Tooltip("Delete Goal"));
        deleteBtn.setOnAction(e -> onDeleteGoal(goal));

        container.getChildren().addAll(info, deleteBtn);
        return container;
    }

    /**
     * Get display name for goal type
     */
    private String getGoalTypeDisplay(String goalType) {
        switch (goalType) {
            case "QUESTIONS": return "Answer Questions";
            case "XP": return "Earn XP";
            case "CHAPTERS": return "Complete Chapters";
            case "TIME": return "Study Time";
            case "COURSES": return "Enroll in Courses";
            case "STREAK": return "Maintain Streak";
            default: return goalType;
        }
    }

    /**
     * Convert display name to goal type code
     */
    private String getGoalTypeCode(String displayName) {
        switch (displayName) {
            case "Answer Questions": return "QUESTIONS";
            case "Earn XP": return "XP";
            case "Complete Chapters": return "CHAPTERS";
            case "Study Time": return "TIME";
            case "Enroll in Courses": return "COURSES";
            case "Maintain Streak": return "STREAK";
            default: return displayName;
        }
    }

    /**
     * Handle add goal button click
     */
    private void onAddGoal() {
        String goalName = goalNameField.getText().trim();
        String goalTypeDisplay = goalTypeCombo.getValue();
        String targetText = targetValueField.getText().trim();

        // Validation
        if (goalName.isEmpty()) {
            showError("Please enter a goal name");
            return;
        }
        if (goalTypeDisplay == null) {
            showError("Please select a goal type");
            return;
        }
        if (targetText.isEmpty()) {
            showError("Please enter a target value");
            return;
        }

        int targetValue;
        try {
            targetValue = Integer.parseInt(targetText);
            if (targetValue <= 0) {
                showError("Target value must be positive");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Target value must be a number");
            return;
        }

        // Create goal
        String goalType = getGoalTypeCode(goalTypeDisplay);
        UserGoal newGoal = new UserGoal(currentUser.getId(), goalName, goalType, targetValue);

        try {
            goalDAO.createGoal(newGoal);
            changesMade = true;

            // Clear fields
            goalNameField.clear();
            goalTypeCombo.setValue(null);
            targetValueField.clear();
            targetUnitLabel.setText("");

            // Reload goals list
            loadExistingGoals();

            showSuccess("Goal added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to add goal: " + e.getMessage());
        }
    }

    /**
     * Handle delete goal
     */
    private void onDeleteGoal(UserGoal goal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Goal");
        confirm.setHeaderText("Are you sure you want to delete this goal?");
        confirm.setContentText(goal.getGoalName());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    goalDAO.deleteGoal(goal.getId());
                    changesMade = true;
                    loadExistingGoals();
                    showSuccess("Goal deleted successfully!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Failed to delete goal: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Handle cancel button
     */
    private void onCancel() {
        closeDialog();
    }

    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Check if changes were made
     */
    public boolean hasChangesMade() {
        return changesMade;
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setOnHidden(e -> alert.close());
        alert.show();
        
        // Auto-close after 2 seconds
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> alert.close());
            } catch (InterruptedException ex) {}
        }).start();
    }
}
