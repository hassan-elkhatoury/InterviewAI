package com.interviewai.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main layout controller that manages the persistent sidebar and dynamic content area
 */
public class MainLayoutController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private SidebarController sidebarController;
    
    private static MainLayoutController instance;
    private Object currentContentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        // Load dashboard content by default
        loadContent("/fxml/DashboardContent.fxml", "dashboard");
    }

    /**
     * Get the singleton instance of this controller
     */
    public static MainLayoutController getInstance() {
        return instance;
    }

    /**
     * Load a view into the content area while keeping the sidebar persistent
     * @param fxmlPath Path to the FXML file (e.g., "/fxml/DashboardContent.fxml", "/fxml/LessonView.fxml")
     * @param activeButton Name of the button to highlight in sidebar (e.g., "dashboard", "progress")
     */
    public void loadContent(String fxmlPath, String activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            
            // Store the controller for later access
            currentContentController = loader.getController();
            
            // Clear current content and add new content
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            
            // Update sidebar active button and lesson button visibility
            if (sidebarController != null) {
                sidebarController.setActiveButton(activeButton);
                sidebarController.updateLessonButtonVisibility();
            }
            
            System.out.println("Loaded content: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Error loading content: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Get the controller of the currently loaded content
     */
    public Object getCurrentContentController() {
        return currentContentController;
    }

    /**
     * Get the sidebar controller
     */
    public SidebarController getSidebarController() {
        return sidebarController;
    }

    /**
     * Get the dashboard controller if currently loaded
     */
    public DashboardController getDashboardController() {
        if (currentContentController instanceof DashboardController) {
            return (DashboardController) currentContentController;
        }
        return null;
    }
}
