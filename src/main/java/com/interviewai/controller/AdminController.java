package com.interviewai.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.interviewai.model.User;
import com.interviewai.service.AdminService;
import com.interviewai.service.AnalyticsService;
import com.interviewai.util.Routes;
import com.interviewai.util.SceneNavigator;
import com.interviewai.util.SessionContext;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminController {
    
    private AdminService adminService;
    private AnalyticsService analyticsService;
    private User currentUser;

    // FXML UI Elements - KPI Cards
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label dailySignupsLabel;
    @FXML private Label weeklySignupsLabel;
    @FXML private Label monthlySignupsLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label interviewsCompletedLabel;
    @FXML private Label averageScoreLabel;
    @FXML private Label mostCommonTypeLabel;
    @FXML private Label mostSelectedLangLabel;

    // Charts
    @FXML private LineChart<String, Number> signupTrendChart;
    @FXML private LineChart<String, Number> usageTrendChart;
    @FXML private PieChart interviewTypePieChart;
    @FXML private PieChart languagePieChart;

    // System Health
    @FXML private Label dbStatusLabel;
    @FXML private Label memoryUsageLabel;
    @FXML private ProgressBar memoryProgressBar;
    @FXML private Label aiResponseTimeLabel;
    @FXML private Label apiUptimeLabel;

    // Sidebar
    @FXML private VBox adminSidebar;
    @FXML private AdminSidebarController adminSidebarController;

    // Other
    @FXML private Button logoutButton;
    @FXML private Label welcomeLabel;
    @FXML private VBox dashboardContainer;

    public AdminController() {
        this.adminService = new AdminService();
        this.analyticsService = new AnalyticsService();
        this.currentUser = SessionContext.getCurrentUser();
    }

    @FXML
    public void initialize() {
        // Set sidebar active page
        if (adminSidebarController != null) {
            adminSidebarController.setActivePage("dashboard");
        }

        // Set welcome message
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        }

        // Load dashboard data
        loadDashboardData();
    }

    /**
     * Load all dashboard metrics and update UI.
     */
    public void loadDashboardData() {
        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            Map<String, Object> metrics = analyticsService.getDashboardMetrics();
            
            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                updateKPICards(metrics);
                updateCharts(metrics);
                updateSystemHealth(metrics);
            });
        }).start();
    }

    /**
     * Update KPI card labels with metrics.
     */
    @SuppressWarnings("unchecked")
    private void updateKPICards(Map<String, Object> metrics) {
        if (totalUsersLabel != null) {
            totalUsersLabel.setText(String.valueOf(metrics.getOrDefault("totalUsers", 0)));
        }
        if (activeUsersLabel != null) {
            activeUsersLabel.setText(String.valueOf(metrics.getOrDefault("activeUsers", 0)));
        }
        if (dailySignupsLabel != null) {
            dailySignupsLabel.setText(String.valueOf(metrics.getOrDefault("dailySignups", 0)));
        }
        if (weeklySignupsLabel != null) {
            weeklySignupsLabel.setText(String.valueOf(metrics.getOrDefault("weeklySignups", 0)));
        }
        if (monthlySignupsLabel != null) {
            monthlySignupsLabel.setText(String.valueOf(metrics.getOrDefault("monthlySignups", 0)));
        }
        if (totalCoursesLabel != null) {
            totalCoursesLabel.setText(String.valueOf(metrics.getOrDefault("totalCourses", 0)));
        }
        if (interviewsCompletedLabel != null) {
            interviewsCompletedLabel.setText(String.valueOf(metrics.getOrDefault("interviewsCompleted", 0)));
        }
        if (averageScoreLabel != null) {
            double avgScore = (Double) metrics.getOrDefault("averageScore", 0.0);
            averageScoreLabel.setText(String.format("%.1f XP", avgScore));
        }
        if (mostCommonTypeLabel != null) {
            mostCommonTypeLabel.setText(String.valueOf(metrics.getOrDefault("mostCommonInterviewType", "N/A")));
        }
        if (mostSelectedLangLabel != null) {
            mostSelectedLangLabel.setText(String.valueOf(metrics.getOrDefault("mostSelectedLanguage", "N/A")));
        }
    }

    /**
     * Update charts with trend and distribution data.
     */
    @SuppressWarnings("unchecked")
    private void updateCharts(Map<String, Object> metrics) {
        // Signup Trend Line Chart
        if (signupTrendChart != null) {
            signupTrendChart.getData().clear();
            XYChart.Series<String, Number> signupSeries = new XYChart.Series<>();
            signupSeries.setName("Signups");
            
            List<Map<String, Object>> signupTrend = (List<Map<String, Object>>) metrics.get("signupTrend");
            if (signupTrend != null) {
                for (Map<String, Object> point : signupTrend) {
                    LocalDate date = (LocalDate) point.get("date");
                    int count = (Integer) point.get("count");
                    signupSeries.getData().add(new XYChart.Data<>(date.toString(), count));
                }
            }
            signupTrendChart.getData().add(signupSeries);
        }

        // Usage Trend Line Chart
        if (usageTrendChart != null) {
            usageTrendChart.getData().clear();
            XYChart.Series<String, Number> usageSeries = new XYChart.Series<>();
            usageSeries.setName("Activity");
            
            List<Map<String, Object>> usageTrend = (List<Map<String, Object>>) metrics.get("usageTrend");
            if (usageTrend != null) {
                for (Map<String, Object> point : usageTrend) {
                    LocalDate date = (LocalDate) point.get("date");
                    int count = (Integer) point.get("count");
                    usageSeries.getData().add(new XYChart.Data<>(date.toString(), count));
                }
            }
            usageTrendChart.getData().add(usageSeries);
        }

        // Interview Type Pie Chart
        if (interviewTypePieChart != null) {
            Map<String, Integer> typeDistribution = (Map<String, Integer>) metrics.get("interviewTypeDistribution");
            if (typeDistribution != null) {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                for (Map.Entry<String, Integer> entry : typeDistribution.entrySet()) {
                    pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
                interviewTypePieChart.setData(pieData);
            }
        }

        // Language Pie Chart
        if (languagePieChart != null) {
            Map<String, Integer> langDistribution = (Map<String, Integer>) metrics.get("languageDistribution");
            if (langDistribution != null) {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                for (Map.Entry<String, Integer> entry : langDistribution.entrySet()) {
                    pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
                languagePieChart.setData(pieData);
            }
        }
    }

    /**
     * Update system health indicators.
     */
    @SuppressWarnings("unchecked")
    private void updateSystemHealth(Map<String, Object> metrics) {
        Map<String, Object> health = (Map<String, Object>) metrics.get("systemHealth");
        if (health == null) return;

        if (dbStatusLabel != null) {
            String dbStatus = (String) health.getOrDefault("dbConnectionStatus", "Unknown");
            dbStatusLabel.setText(dbStatus);
            dbStatusLabel.setStyle(dbStatus.equals("Healthy") ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        }
        if (memoryUsageLabel != null) {
            memoryUsageLabel.setText((String) health.getOrDefault("memoryUsage", "N/A"));
        }
        if (memoryProgressBar != null) {
            int memPercent = (Integer) health.getOrDefault("memoryPercent", 0);
            memoryProgressBar.setProgress(memPercent / 100.0);
        }
        if (aiResponseTimeLabel != null) {
            aiResponseTimeLabel.setText((String) health.getOrDefault("aiResponseTime", "N/A"));
        }
        if (apiUptimeLabel != null) {
            apiUptimeLabel.setText((String) health.getOrDefault("apiUptime", "N/A"));
        }
    }

    @FXML
    public void onRefresh(ActionEvent event) {
        loadDashboardData();
    }

    // ==================== User Management Methods ====================

    public boolean login(String username, String password) {
        User user = adminService.login(username, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public void createAdmin(String username, String email, String password, String role) {
        if (currentUser != null && currentUser.hasPermission("manage_users")) {
            boolean success = adminService.createAdmin(username, email, password, role);
            if (success) {
                showAlert("Success", "Admin created successfully.");
            } else {
                showAlert("Error", "Failed to create admin.");
            }
        } else {
            showAlert("Access Denied", "You do not have permission to perform this action.");
        }
    }

    public java.util.List<User> getAllUsers() {
        if (currentUser != null && currentUser.hasPermission("manage_users")) {
            return adminService.getAllUsers();
        }
        return new java.util.ArrayList<>();
    }

    public boolean updateUserRole(User user, String newRole) {
        if (currentUser != null && currentUser.hasPermission("manage_users")) {
            return adminService.updateAdminRole(user.getId(), newRole);
        }
        return false;
    }

    public boolean toggleUserStatus(User user) {
        if (currentUser != null && currentUser.hasPermission("manage_users")) {
            return adminService.toggleAdminAccess(user.getId(), !user.isActive());
        }
        return false;
    }

    public boolean resetUserPassword(User user, String newPassword) {
        if (currentUser != null && currentUser.hasPermission("manage_users")) {
            return adminService.resetPassword(user.getId(), newPassword);
        }
        return false;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
