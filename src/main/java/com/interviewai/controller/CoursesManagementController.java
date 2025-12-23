package com.interviewai.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.interviewai.dao.CourseManagementDAO;
import com.interviewai.model.CourseManagementDTO;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Controller for the Admin Courses Management page.
 * Provides functionality to view, search, filter, and manage all courses.
 */
public class CoursesManagementController implements Initializable {

    // Sidebar controller (injected via fx:include)
    @FXML private AdminSidebarController adminSidebarController;

    // Header statistics
    @FXML private Label totalCoursesLabel;
    @FXML private Label activeCoursesLabel;
    @FXML private Label completedCoursesLabel;
    @FXML private Label inProgressCoursesLabel;
    @FXML private Label avgCompletionLabel;

    // Table count badge
    @FXML private Label tableCountBadge;

    // Search and filter controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button searchBtn;
    @FXML private Button resetBtn;
    @FXML private Button refreshBtn;

    // Table view
    @FXML private TableView<CourseManagementDTO> coursesTable;
    @FXML private TableColumn<CourseManagementDTO, Integer> idColumn;
    @FXML private TableColumn<CourseManagementDTO, String> titleColumn;
    @FXML private TableColumn<CourseManagementDTO, String> userColumn;
    @FXML private TableColumn<CourseManagementDTO, String> statusColumn;
    @FXML private TableColumn<CourseManagementDTO, String> progressColumn;
    @FXML private TableColumn<CourseManagementDTO, Integer> chaptersColumn;
    @FXML private TableColumn<CourseManagementDTO, Integer> questionsColumn;
    @FXML private TableColumn<CourseManagementDTO, String> createdColumn;
    @FXML private TableColumn<CourseManagementDTO, Void> actionsColumn;

    // Pagination
    @FXML private Label pageInfoLabel;

    // Data
    private final CourseManagementDAO courseDAO = new CourseManagementDAO();
    private final ObservableList<CourseManagementDTO> coursesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set sidebar active page
        if (adminSidebarController != null) {
            adminSidebarController.setActivePage("courses");
        }

        // Initialize table columns
        setupTableColumns();

        // Load status filter options
        setupStatusFilter();

        // Load initial data
        loadCourses();
        loadStatistics();

        // Setup search listener
        if (searchField != null) {
            searchField.setOnAction(e -> onSearch());
        }
    }

    private void setupTableColumns() {
        // ID Column
        if (idColumn != null) {
            idColumn.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        }

        // Title Column
        if (titleColumn != null) {
            titleColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getCourseTitle()));
        }

        // User Column (username + email)
        if (userColumn != null) {
            userColumn.setCellValueFactory(cellData -> {
                CourseManagementDTO course = cellData.getValue();
                String display = course.getUsername();
                if (course.getUserEmail() != null && !course.getUserEmail().isEmpty()) {
                    display += "\n" + course.getUserEmail();
                }
                return new SimpleStringProperty(display);
            });
        }

        // Status Column with styled badges
            if (statusColumn != null) {
                statusColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getStatus()));

                statusColumn.setCellFactory(column -> new TableCell<>() {

                    private final Label statusLabel = new Label();
                    private final HBox wrapper = new HBox(statusLabel);

                    {
                        wrapper.setAlignment(Pos.CENTER); // CENTER horizontally & vertically
                    }

                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);

                        if (empty || status == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            statusLabel.setText(status);

                            String baseStyle =
                                    "-fx-font-size: 9px;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 4 10;" +
                                    "-fx-background-radius: 12;";

                            switch (status.toUpperCase()) {
                                case "ACTIVE":
                                    statusLabel.setStyle(baseStyle +
                                            "-fx-text-fill: #22C55E;" +
                                            "-fx-background-color: rgba(34,197,94,0.15);");
                                    break;

                                case "COMPLETE":
                                case "COMPLETED":
                                    statusLabel.setStyle(baseStyle +
                                            "-fx-text-fill: #3B82F6;" +
                                            "-fx-background-color: rgba(59,130,246,0.15);");
                                    break;

                                case "ARCHIVED":
                                    statusLabel.setStyle(baseStyle +
                                            "-fx-text-fill: #6B7280;" +
                                            "-fx-background-color: rgba(107,114,128,0.15);");
                                    break;

                                default:
                                    statusLabel.setStyle(baseStyle +
                                            "-fx-text-fill: #F59E0B;" +
                                            "-fx-background-color: rgba(245,158,11,0.15);");
                            }

                            setGraphic(wrapper); 
                            setText(null);       
                        }
                    }
                });
            }


        // Progress Column
        if (progressColumn != null) {
            progressColumn.setCellValueFactory(cellData -> {
                CourseManagementDTO course = cellData.getValue();
                return new SimpleStringProperty(course.getProgressDisplay() + " (" + course.getFormattedCompletion() + ")");
            });
        }

        // Chapters Column
        if (chaptersColumn != null) {
            chaptersColumn.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getChaptersCount()).asObject());
        }

        // Questions Column
        if (questionsColumn != null) {
            questionsColumn.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getQuestionsCount()).asObject());
        }

        // Created Date Column
        if (createdColumn != null) {
            createdColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        }

        // Actions Column
        if (actionsColumn != null) {
            actionsColumn.setCellFactory(column -> new TableCell<>() {
                private final Button viewBtn = new Button("👁️");
                private final Button archiveBtn = new Button("📦");
                private final Button deleteBtn = new Button("🗑️");
                private final HBox actionBox = new HBox(6, viewBtn, archiveBtn, deleteBtn);

                {
                    // Style buttons
                    String btnStyle = "-fx-background-color: rgba(255,255,255,0.08); " +
                                     "-fx-text-fill: #9CA3AF; -fx-cursor: hand; " +
                                     "-fx-padding: 6 10; -fx-background-radius: 6;";
                    viewBtn.setStyle(btnStyle);
                    archiveBtn.setStyle(btnStyle);
                    deleteBtn.setStyle(btnStyle + "-fx-text-fill: #EF4444;");

                    viewBtn.setOnAction(e -> {
                        CourseManagementDTO course = getTableView().getItems().get(getIndex());
                        onViewCourse(course);
                    });

                    archiveBtn.setOnAction(e -> {
                        CourseManagementDTO course = getTableView().getItems().get(getIndex());
                        onToggleArchive(course);
                    });

                    deleteBtn.setOnAction(e -> {
                        CourseManagementDTO course = getTableView().getItems().get(getIndex());
                        onDeleteCourse(course);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(actionBox);
                    }
                }
            });
        }

        // Set table items
        if (coursesTable != null) {
            coursesTable.setItems(coursesList);
        }
    }

    private void setupStatusFilter() {
        if (statusFilter != null) {
            ObservableList<String> options = FXCollections.observableArrayList();
            options.add("All Statuses");
            
            // Get distinct statuses from database
            List<String> statuses = courseDAO.getDistinctStatuses();
            options.addAll(statuses);
            
            statusFilter.setItems(options);
            statusFilter.getSelectionModel().selectFirst();
            
            // Add listener for filter changes
            statusFilter.setOnAction(e -> applyFilters());
        }
    }

    private void loadCourses() {
        coursesList.clear();
        List<CourseManagementDTO> courses = courseDAO.getAllCourses();
        coursesList.addAll(courses);
        updatePageInfo();
    }

    private void loadStatistics() {
        int[] stats = courseDAO.getCourseStatistics();
        
        if (totalCoursesLabel != null) {
            totalCoursesLabel.setText(String.valueOf(stats[0]));
        }
        if (activeCoursesLabel != null) {
            activeCoursesLabel.setText(String.valueOf(stats[1]));
        }
        if (completedCoursesLabel != null) {
            completedCoursesLabel.setText(String.valueOf(stats[2]));
        }
        if (inProgressCoursesLabel != null) {
            // Calculate in-progress as total - active - completed
            int inProgress = stats[0] - stats[1] - stats[2];
            inProgressCoursesLabel.setText(String.valueOf(Math.max(0, inProgress)));
        }
        if (avgCompletionLabel != null) {
            // Calculate average completion percentage
            avgCompletionLabel.setText(stats[5] + "%");
        }
    }

    private void updatePageInfo() {
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Showing " + coursesList.size() + " courses");
        }
        if (tableCountBadge != null) {
            tableCountBadge.setText(String.valueOf(coursesList.size()));
        }
    }

    // ============== Event Handlers ==============

    @FXML
    public void onSearch() {
        applyFilters();
    }

    @FXML
    public void onResetFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (statusFilter != null) {
            statusFilter.getSelectionModel().selectFirst();
        }
        loadCourses();
    }

    @FXML
    public void onRefresh() {
        loadCourses();
        loadStatistics();
        showNotification("Data refreshed successfully");
    }

    private void applyFilters() {
        String searchTerm = searchField != null ? searchField.getText().trim() : "";
        String selectedStatus = statusFilter != null ? statusFilter.getValue() : "All Statuses";
        
        coursesList.clear();
        List<CourseManagementDTO> courses;
        
        if (!searchTerm.isEmpty()) {
            courses = courseDAO.searchCourses(searchTerm);
        } else if (selectedStatus != null && !"All Statuses".equals(selectedStatus)) {
            courses = courseDAO.getCoursesByStatus(selectedStatus);
        } else {
            courses = courseDAO.getAllCourses();
        }
        
        // Apply status filter if both search and status are set
        if (!searchTerm.isEmpty() && selectedStatus != null && !"All Statuses".equals(selectedStatus)) {
            courses = courses.stream()
                .filter(c -> selectedStatus.equalsIgnoreCase(c.getStatus()))
                .toList();
        }
        
        coursesList.addAll(courses);
        updatePageInfo();
    }

    private void onViewCourse(CourseManagementDTO course) {
        // Show course details dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Course Details");
        alert.setHeaderText(course.getCourseTitle());
        
        String content = String.format(
            "Course ID: %d\n" +
            "Owner: %s (%s)\n" +
            "Status: %s\n" +
            "Chapters: %s\n" +
            "Total Questions: %d\n" +
            "Progress: %s\n" +
            "Created: %s",
            course.getId(),
            course.getUsername(),
            course.getUserEmail(),
            course.getStatus(),
            course.getProgressDisplay(),
            course.getQuestionsCount(),
            course.getFormattedCompletion(),
            course.getFormattedDate()
        );
        
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void onToggleArchive(CourseManagementDTO course) {
        String currentStatus = course.getStatus();
        String newStatus = "ARCHIVED".equalsIgnoreCase(currentStatus) ? "ACTIVE" : "ARCHIVED";
        String action = "ARCHIVED".equalsIgnoreCase(currentStatus) ? "restore" : "archive";
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm " + (action.equals("archive") ? "Archive" : "Restore"));
        confirm.setHeaderText("Are you sure you want to " + action + " this course?");
        confirm.setContentText(course.getCourseTitle());
        styleAlert(confirm);
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = courseDAO.updateCourseStatus(course.getId(), newStatus);
            if (success) {
                showNotification("Course " + action + "d successfully");
                loadCourses();
                loadStatistics();
            } else {
                showError("Failed to " + action + " course");
            }
        }
    }

    private void onDeleteCourse(CourseManagementDTO course) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete this course?");
        confirm.setContentText("Course: " + course.getCourseTitle() + 
                              "\nThis will also delete all chapters and questions.\nThis action cannot be undone.");
        styleAlert(confirm);
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = courseDAO.deleteCourse(course.getId());
            if (success) {
                showNotification("Course deleted successfully");
                loadCourses();
                loadStatistics();
            } else {
                showError("Failed to delete course");
            }
        }
    }

    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        // Apply dark theme styling to alerts
        alert.getDialogPane().setStyle(
            "-fx-background-color: #0D1117; " +
            "-fx-border-color: rgba(255,255,255,0.1); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        alert.getDialogPane().lookup(".content.label").setStyle(
            "-fx-text-fill: #E5E7EB; -fx-font-size: 13px;"
        );
    }
}
