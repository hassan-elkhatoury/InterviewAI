package com.interviewai.controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.interviewai.dao.UserDAO;
import com.interviewai.dao.UserManagementDAO;
import com.interviewai.dao.RoleDAO;
import com.interviewai.dao.ProgressDAO;
import com.interviewai.model.User;
import com.interviewai.model.UserManagementDTO;
import com.interviewai.model.Role;
import com.interviewai.model.OnboardingData;
import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;
import com.interviewai.util.SessionContext;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import java.awt.Desktop;

/**
 * Controller for User Management in Admin Panel.
 * Handles user directory, search, filtering, and admin actions.
 */
public class UserManagementController {

    // =============== FXML UI Components ===============
    
    // Search & Filter Controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterInterviewType;
    @FXML private ComboBox<String> filterLanguage;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterProgress;
    @FXML private Button clearFiltersBtn;
    
    // User Directory Table
    @FXML private TableView<UserManagementDTO> userTable;
    @FXML private TableColumn<UserManagementDTO, Integer> colId;
    @FXML private TableColumn<UserManagementDTO, String> colUsername;
    @FXML private TableColumn<UserManagementDTO, String> colEmail;
    @FXML private TableColumn<UserManagementDTO, String> colRole;
    @FXML private TableColumn<UserManagementDTO, String> colStatus;
    @FXML private TableColumn<UserManagementDTO, String> colInterviewType;
    @FXML private TableColumn<UserManagementDTO, String> colLanguage;
    @FXML private TableColumn<UserManagementDTO, Integer> colXP;
    @FXML private TableColumn<UserManagementDTO, String> colLastActive;
    @FXML private TableColumn<UserManagementDTO, Void> colActions;
    
    // Pagination
    @FXML private Label totalUsersLabel;
    @FXML private Label activeTodayLabel;
    @FXML private Label pageLabel;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    
    // User Profile Panel
    @FXML private VBox profilePanel;
    @FXML private Label avatarInitials;
    @FXML private Label profileName;
    @FXML private Label profileEmail;
    @FXML private Label profileStatus;
    @FXML private Label profileRole;
    
    // Quick Stats
    @FXML private Label statXP;
    @FXML private Label statStreak;
    @FXML private Label statBadges;
    @FXML private Label statLessons;
    
    // Info Labels
    @FXML private Label infoInterviewType;
    @FXML private Label infoLanguage;
    @FXML private Label infoTimeline;
    @FXML private Label infoContext;
    @FXML private Label infoUserId;
    @FXML private Label infoJoined;
    @FXML private Label infoLastActive;
    @FXML private Label info2FA;
    
    // Progress Labels
    @FXML private Label progressLessons;
    @FXML private Label progressQuizScore;
    @FXML private Label progressSimScore;
    @FXML private Label progressCourses;
    
    // Badges
    @FXML private FlowPane badgesPane;
    
    // Action Buttons
    @FXML private Button suspendBtn;
    @FXML private Button activateBtn;
    @FXML private Button resetXPBtn;
    @FXML private Button resetProgressBtn;
    @FXML private Button editProfileBtn;
    
    // Sidebar
    @FXML private VBox adminSidebar;
    @FXML private AdminSidebarController adminSidebarController;
    
    // =============== Data & Services ===============
    private UserManagementDAO userManagementDAO;
    private UserDAO userDAO;
    private RoleDAO roleDAO;
    private User currentAdmin;
    private UserManagementDTO selectedUser;
    
    private ObservableList<UserManagementDTO> allUsers = FXCollections.observableArrayList();
    private FilteredList<UserManagementDTO> filteredUsers;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    
    public UserManagementController() {
        this.userManagementDAO = new UserManagementDAO();
        this.userDAO = new UserDAO();
        this.roleDAO = new RoleDAO();
        this.currentAdmin = SessionContext.getCurrentUser();
    }
    
    @FXML
    public void initialize() {
        // Set sidebar active page
        if (adminSidebarController != null) {
            adminSidebarController.setActivePage("users");
        }
        
        // Setup filters
        setupFilters();
        
        // Setup table columns
        setupTableColumns();
        
        // Load users
        loadUsers();
        
        // Setup search functionality
        setupSearch();
        
        // Setup button handlers
        setupButtonHandlers();
        
        // Setup table selection listener
        if (userTable != null) {
            userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedUser = newSelection;
                        showUserProfile(newSelection);
                    }
                }
            );
        }
    }
    
    private void setupFilters() {
        if (filterInterviewType != null) {
            filterInterviewType.setItems(FXCollections.observableArrayList(
                "All Types", "JOB", "VISA", "INTERNSHIP", "UNIVERSITY"
            ));
            filterInterviewType.setValue("All Types");
            filterInterviewType.setOnAction(e -> applyFilters());
        }
        
        if (filterLanguage != null) {
            filterLanguage.setItems(FXCollections.observableArrayList(
                "All Languages", "ENGLISH", "FRENCH", "ARABIC", "SPANISH"
            ));
            filterLanguage.setValue("All Languages");
            filterLanguage.setOnAction(e -> applyFilters());
        }
        
        if (filterStatus != null) {
            filterStatus.setItems(FXCollections.observableArrayList(
                "All Status", "Active", "Suspended"
            ));
            filterStatus.setValue("All Status");
            filterStatus.setOnAction(e -> applyFilters());
        }
        
        if (filterProgress != null) {
            filterProgress.setItems(FXCollections.observableArrayList(
                "All Levels", "Beginner (0-100 XP)", "Intermediate (100-500 XP)", "Advanced (500+ XP)"
            ));
            filterProgress.setValue("All Levels");
            filterProgress.setOnAction(e -> applyFilters());
        }
    }
    
    private void setupTableColumns() {
        if (colId != null) {
            colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        }
        if (colUsername != null) {
            colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        }
        if (colEmail != null) {
            colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        }
        if (colRole != null) {
            colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
            colRole.setCellFactory(col -> new TableCell<UserManagementDTO, String>() {
                @Override
                protected void updateItem(String role, boolean empty) {
                    super.updateItem(role, empty);
                    if (empty || role == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label badge = new Label(role);
                        badge.getStyleClass().add(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("SUPER_ADMIN") 
                            ? "role-admin-cell" : "role-user-cell");
                        setGraphic(badge);
                        setText(null);
                    }
                }
            });
        }
        if (colStatus != null) {
            colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusString()));
            colStatus.setCellFactory(col -> new TableCell<UserManagementDTO, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label badge = new Label(status);
                        badge.getStyleClass().add("Active".equals(status) ? "status-active" : "status-suspended");
                        setGraphic(badge);
                        setText(null);
                    }
                }
            });
        }
        if (colInterviewType != null) {
            colInterviewType.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getInterviewType() != null ? data.getValue().getInterviewType() : "N/A"));
        }
        if (colLanguage != null) {
            colLanguage.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLanguage() != null ? data.getValue().getLanguage() : "N/A"));
        }
        if (colXP != null) {
            colXP.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTotalXP()).asObject());
        }
        if (colLastActive != null) {
            colLastActive.setCellValueFactory(data -> {
                Timestamp lastActive = data.getValue().getLastActive();
                if (lastActive != null) {
                    return new SimpleStringProperty(lastActive.toLocalDateTime().format(DATE_FORMATTER));
                }
                return new SimpleStringProperty("N/A");
            });
        }
        
        // Setup actions column
        if (colActions != null) {
            colActions.setCellFactory(col -> new TableCell<UserManagementDTO, Void>() {
                private final Button viewBtn = new Button();
                private final HBox btnContainer = new HBox();
                
                {
                    // Create modern view button with icon and text
                    Label icon = new Label("👁");
                    icon.getStyleClass().add("view-btn-icon");
                    Label text = new Label("View");
                    text.getStyleClass().add("view-btn-text");
                    
                    HBox content = new HBox(4);
                    content.setAlignment(Pos.CENTER);
                    content.getChildren().addAll(icon, text);
                    
                    viewBtn.setGraphic(content);
                    viewBtn.getStyleClass().add("table-view-btn");
                    viewBtn.setOnAction(e -> {
                        UserManagementDTO user = getTableView().getItems().get(getIndex());
                        showUserProfile(user);
                    });
                    
                    btnContainer.setAlignment(Pos.CENTER);
                    btnContainer.getChildren().add(viewBtn);
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btnContainer);
                }
            });
        }
    }
    
    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }
    
    private void setupButtonHandlers() {
        if (clearFiltersBtn != null) clearFiltersBtn.setOnAction(this::onClearFilters);
        if (suspendBtn != null) suspendBtn.setOnAction(this::onSuspendUser);
        if (activateBtn != null) activateBtn.setOnAction(this::onActivateUser);
        if (resetXPBtn != null) resetXPBtn.setOnAction(this::onResetXP);
        if (resetProgressBtn != null) resetProgressBtn.setOnAction(this::onResetProgress);
        if (editProfileBtn != null) editProfileBtn.setOnAction(this::onEditProfile);
    }
    
    private void loadUsers() {
        new Thread(() -> {
            try {
                List<UserManagementDTO> users = userManagementDAO.getAllUsersWithDetails();
                
                Platform.runLater(() -> {
                    allUsers.clear();
                    allUsers.addAll(users);
                    
                    filteredUsers = new FilteredList<>(allUsers, p -> true);
                    userTable.setItems(filteredUsers);
                    
                    // Update total count
                    if (totalUsersLabel != null) {
                        totalUsersLabel.setText("Showing " + users.size() + " users");
                    }
                                        // Update total count
                    if (activeTodayLabel != null) {
                        activeTodayLabel.setText(users.size()+"");
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Failed to load users: " + e.getMessage()));
            }
        }).start();
    }
    
    private void applyFilters() {
        if (filteredUsers == null) return;
        
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        String interviewType = filterInterviewType != null ? filterInterviewType.getValue() : "All Types";
        String language = filterLanguage != null ? filterLanguage.getValue() : "All Languages";
        String status = filterStatus != null ? filterStatus.getValue() : "All Status";
        String progress = filterProgress != null ? filterProgress.getValue() : "All Levels";
        
        filteredUsers.setPredicate(user -> {
            // Search filter
            if (!searchText.isEmpty()) {
                boolean matchesSearch = user.getUsername().toLowerCase().contains(searchText) ||
                                       (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText));
                if (!matchesSearch) return false;
            }
            
            // Interview type filter
            if (!"All Types".equals(interviewType)) {
                if (user.getInterviewType() == null || !interviewType.equals(user.getInterviewType())) return false;
            }
            
            // Language filter
            if (!"All Languages".equals(language)) {
                if (user.getLanguage() == null || !language.equals(user.getLanguage())) return false;
            }
            
            // Status filter
            if (!"All Status".equals(status)) {
                String userStatus = user.isActive() ? "Active" : "Suspended";
                if (!status.equals(userStatus)) return false;
            }
            
            // Progress level filter
            if (!"All Levels".equals(progress)) {
                int xp = user.getTotalXP();
                switch (progress) {
                    case "Beginner (0-100 XP)":
                        if (xp > 100) return false;
                        break;
                    case "Intermediate (100-500 XP)":
                        if (xp <= 100 || xp > 500) return false;
                        break;
                    case "Advanced (500+ XP)":
                        if (xp <= 500) return false;
                        break;
                }
            }
            
            return true;
        });
        
        // Update count
        if (totalUsersLabel != null) {
            totalUsersLabel.setText("Showing " + filteredUsers.size() + " users");
        }
        if (activeTodayLabel != null) {
            activeTodayLabel.setText(filteredUsers.size()+"");
        }
    }
    
    @FXML
    public void onClearFilters(ActionEvent event) {
        if (searchField != null) searchField.clear();
        if (filterInterviewType != null) filterInterviewType.setValue("All Types");
        if (filterLanguage != null) filterLanguage.setValue("All Languages");
        if (filterStatus != null) filterStatus.setValue("All Status");
        if (filterProgress != null) filterProgress.setValue("All Levels");
        applyFilters();
    }
    
    private void showUserProfile(UserManagementDTO user) {
        selectedUser = user;
        
        // Enable action buttons
        enableActionButtons(true);
        
        // Update avatar and basic info
        if (avatarInitials != null) avatarInitials.setText(user.getInitials());
        if (profileName != null) profileName.setText(user.getUsername());
        if (profileEmail != null) profileEmail.setText(user.getEmail());
        
        // Update status badge - make visible
        if (profileStatus != null) {
            profileStatus.setText(user.getStatusString());
            profileStatus.setVisible(true);
            profileStatus.setManaged(true);
            profileStatus.getStyleClass().removeAll("suspended");
            if (!user.isActive()) {
                profileStatus.getStyleClass().add("suspended");
            }
        }
        
        // Update role badge - make visible
        if (profileRole != null) {
            profileRole.setText(user.getRole());
            profileRole.setVisible(true);
            profileRole.setManaged(true);
            profileRole.getStyleClass().removeAll("admin");
            if ("ADMIN".equalsIgnoreCase(user.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
                profileRole.getStyleClass().add("admin");
            }
        }
        
        // Update quick stats
        if (statXP != null) statXP.setText(String.valueOf(user.getTotalXP()));
        if (statStreak != null) statStreak.setText(String.valueOf(user.getCurrentStreak()));
        if (statBadges != null) statBadges.setText(String.valueOf(user.getBadgeCount()));
        if (statLessons != null) statLessons.setText(String.valueOf(user.getCompletedLessons()));
        
        // Update interview details
        if (infoInterviewType != null) infoInterviewType.setText(user.getInterviewType() != null ? user.getInterviewType() : "-");
        if (infoLanguage != null) infoLanguage.setText(user.getLanguage() != null ? user.getLanguage() : "-");
        if (infoTimeline != null) infoTimeline.setText(user.getTimeline() != null ? user.getTimeline() : "-");
        if (infoContext != null) infoContext.setText(user.getContext() != null ? user.getContext() : "-");
        
        // Update account details
        if (infoUserId != null) infoUserId.setText(String.valueOf(user.getId()));
        if (infoJoined != null && user.getCreatedAt() != null) {
            infoJoined.setText(user.getCreatedAt().toLocalDateTime().format(DATE_FORMATTER));
        }
        if (infoLastActive != null && user.getLastActive() != null) {
            infoLastActive.setText(user.getLastActive().toLocalDateTime().format(DATE_FORMATTER));
        }
        if (info2FA != null) info2FA.setText(user.isTwoFactorEnabled() ? "Enabled" : "Disabled");
        
        // Update progress details
        if (progressLessons != null) progressLessons.setText(String.valueOf(user.getCompletedLessons()));
        if (progressQuizScore != null) progressQuizScore.setText(String.format("%.0f%%", user.getQuizAvgScore()));
        if (progressSimScore != null) progressSimScore.setText(String.valueOf(user.getSimulationScore()));
        if (progressCourses != null) progressCourses.setText(String.valueOf(user.getEnrolledCourses()));
        
        // Update suspend/activate buttons
        updateSuspendActivateButtons(user.isActive());
        
        // Load badges
        loadUserBadges(user.getId());
    }
    
    private void enableActionButtons(boolean enabled) {
        if (suspendBtn != null) suspendBtn.setDisable(!enabled);
        if (activateBtn != null) activateBtn.setDisable(!enabled);
        if (resetXPBtn != null) resetXPBtn.setDisable(!enabled);
        if (resetProgressBtn != null) resetProgressBtn.setDisable(!enabled);
        if (editProfileBtn != null) editProfileBtn.setDisable(!enabled);
    }
    
    private void updateSuspendActivateButtons(boolean isActive) {
        if (suspendBtn != null) {
            suspendBtn.setVisible(isActive);
            suspendBtn.setManaged(isActive);
        }
        if (activateBtn != null) {
            activateBtn.setVisible(!isActive);
            activateBtn.setManaged(!isActive);
        }
    }
    
    private void loadUserBadges(int userId) {
        if (badgesPane == null) return;
        
        new Thread(() -> {
            try {
                List<String> badges = userManagementDAO.getUserBadges(userId);
                Platform.runLater(() -> {
                    badgesPane.getChildren().clear();
                    if (badges.isEmpty()) {
                        badgesPane.getChildren().add(new Label("No badges yet"));
                    } else {
                        for (String badge : badges) {
                            Label badgeLabel = new Label("🏆 " + badge);
                            badgeLabel.getStyleClass().add("badge-item");
                            badgesPane.getChildren().add(badgeLabel);
                        }
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    public void onSuspendUser(ActionEvent event) {
        if (selectedUser == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Suspension");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to suspend user '" + selectedUser.getUsername() + "'?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userManagementDAO.suspendUser(selectedUser.getId());
                selectedUser.setActive(false);
                userTable.refresh();
                showUserProfile(selectedUser);
                showSuccess("User suspended successfully!");
            } catch (SQLException e) {
                showError("Failed to suspend user: " + e.getMessage());
            }
        }
    }
    
    @FXML
    public void onActivateUser(ActionEvent event) {
        if (selectedUser == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Activation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to activate user '" + selectedUser.getUsername() + "'?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userManagementDAO.activateUser(selectedUser.getId());
                selectedUser.setActive(true);
                userTable.refresh();
                showUserProfile(selectedUser);
                showSuccess("User activated successfully!");
            } catch (SQLException e) {
                showError("Failed to activate user: " + e.getMessage());
            }
        }
    }
    
    @FXML
    public void onResetXP(ActionEvent event) {
        if (selectedUser == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset XP");
        confirm.setHeaderText("Reset XP for " + selectedUser.getUsername());
        confirm.setContentText("This will reset all XP to zero. This cannot be undone!");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userManagementDAO.resetUserXP(selectedUser.getId());
                selectedUser.setTotalXP(0);
                userTable.refresh();
                showUserProfile(selectedUser);
                showSuccess("XP reset successfully!");
            } catch (SQLException e) {
                showError("Failed to reset XP: " + e.getMessage());
            }
        }
    }
    
    @FXML
    public void onResetProgress(ActionEvent event) {
        if (selectedUser == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Progress");
        confirm.setHeaderText("Reset All Progress for " + selectedUser.getUsername());
        confirm.setContentText("This will reset all XP, course progress, and chapter completions. This cannot be undone!");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userManagementDAO.resetUserProgress(selectedUser.getId());
                selectedUser.setTotalXP(0);
                selectedUser.setCompletedLessons(0);
                userTable.refresh();
                showUserProfile(selectedUser);
                showSuccess("Progress reset successfully!");
            } catch (SQLException e) {
                showError("Failed to reset progress: " + e.getMessage());
            }
        }
    }
    
    @FXML
    public void onEditProfile(ActionEvent event) {
        if (selectedUser == null) return;
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + selectedUser.getUsername());
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        TextField emailField = new TextField(selectedUser.getEmail());
        ComboBox<String> roleCombo = new ComboBox<>();
        try {
            List<Role> roles = roleDAO.getAllRoles();
            for (Role role : roles) {
                roleCombo.getItems().add(role.getName());
            }
        } catch (SQLException e) {
            roleCombo.getItems().addAll("CANDIDATE", "ADMIN", "SUPER_ADMIN");
        }
        roleCombo.setValue(selectedUser.getRole());
        
        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Role:"), 0, 1);
        grid.add(roleCombo, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Update email
                if (!emailField.getText().equals(selectedUser.getEmail())) {
                    userManagementDAO.updateUserEmail(selectedUser.getId(), emailField.getText());
                    selectedUser.setEmail(emailField.getText());
                }
                
                // Update role if changed
                if (!selectedUser.getRole().equals(roleCombo.getValue())) {
                    userManagementDAO.updateUserRole(selectedUser.getId(), roleCombo.getValue());
                    selectedUser.setRole(roleCombo.getValue());
                }
                
                userTable.refresh();
                showUserProfile(selectedUser);
                showSuccess("User updated successfully!");
            } catch (SQLException e) {
                showError("Failed to update user: " + e.getMessage());
            }
        }
    }
    
    // =============== Pagination ===============
    
    @FXML
    public void onPrevPage(ActionEvent event) {
        // TODO: Implement pagination - previous page
    }
    
    @FXML
    public void onNextPage(ActionEvent event) {
        // TODO: Implement pagination - next page
    }
    
    private void navigateTo(String route) {
        try {
            Stage stage = (Stage) userTable.getScene().getWindow();
            SceneNavigator.switchTo(stage, route, stage.getWidth(), stage.getHeight());
        } catch (IOException e) {
            showError("Navigation failed: " + e.getMessage());
        }
    }
    
    // =============== Helpers ===============
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
