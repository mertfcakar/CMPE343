package com.group12.greengrocer.controllers;

import com.group12.greengrocer.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for owner interface
 */
public class OwnerController {
    
    private User currentUser;
    
    @FXML
    private Label usernameLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label totalProductsLabel;
    
    @FXML
    private Label activeOrdersLabel;
    
    @FXML
    private Label totalRevenueLabel;
    
    @FXML
    private Label activeCarriersLabel;
    
    @FXML
    private TableView recentOrdersTable;
    
    @FXML
    private TableView productsTable;
    
    @FXML
    private TableView ordersTable;
    
    @FXML
    private TableView carriersTable;
    
    @FXML
    private TableView couponsTable;
    
    @FXML
    private TextField productSearchField;
    
    @FXML
    private TextField minOrdersField;
    
    @FXML
    private TextField loyaltyDiscountField;
    
    @FXML
    private TextField minCartValueField;
    
    @FXML
    private TextField vatRateField;
    
    @FXML
    private ComboBox orderStatusFilter;
    
    @FXML
    private ComboBox reportTypeCombo;
    
    @FXML
    private Label ordersCountLabel;
    
    @FXML
    private ListView messagesListView;
    
    @FXML
    private Label messageFromLabel;
    
    @FXML
    private Label messageSubjectLabel;
    
    @FXML
    private Label messageDateLabel;
    
    @FXML
    private TextArea messageContentArea;
    
    @FXML
    private VBox reportContentBox;
    
    /**
     * Initialize controller with user data
     */
    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Owner: " + user.getUsername());
        loadDashboard();
    }
    
    @FXML
    private void initialize() {
        statusLabel.setText("Ready");
        // Initialize statistics
        totalProductsLabel.setText("0");
        activeOrdersLabel.setText("0");
        totalRevenueLabel.setText("₺0.00");
        activeCarriersLabel.setText("0");
    }
    
    private void loadDashboard() {
        // TODO: Load dashboard statistics from database
        statusLabel.setText("Loading dashboard...");
    }
    
    // Product Management
    @FXML
    private void handleAddProduct() {
        statusLabel.setText("Opening add product dialog...");
        // TODO: Open add product dialog
    }
    
    @FXML
    private void handleUpdateProduct() {
        statusLabel.setText("Opening update product dialog...");
        // TODO: Open update product dialog
    }
    
    @FXML
    private void handleRemoveProduct() {
        statusLabel.setText("Removing product...");
        // TODO: Remove selected product
    }
    
    @FXML
    private void handleSearchProducts() {
        String keyword = productSearchField.getText();
        statusLabel.setText("Searching for: " + keyword);
        // TODO: Search products
    }
    
    // Employee Management
    @FXML
    private void handleHireCarrier() {
        statusLabel.setText("Opening hire carrier dialog...");
        // TODO: Open hire carrier dialog
    }
    
    @FXML
    private void handleFireCarrier() {
        statusLabel.setText("Removing carrier...");
        // TODO: Remove selected carrier
    }
    
    @FXML
    private void handleViewCarrierRatings() {
        statusLabel.setText("Loading carrier ratings...");
        // TODO: Display carrier ratings
    }
    
    // Order Management
    @FXML
    private void handleRefreshOrders() {
        statusLabel.setText("Refreshing orders...");
        // TODO: Reload orders
    }
    
    // Customer Communication
    @FXML
    private void handleRefreshMessages() {
        statusLabel.setText("Refreshing messages...");
        // TODO: Reload messages
    }
    
    @FXML
    private void handleReplyMessage() {
        statusLabel.setText("Opening reply dialog...");
        // TODO: Open reply message dialog
    }
    
    @FXML
    private void handleDeleteMessage() {
        statusLabel.setText("Deleting message...");
        // TODO: Delete message
    }
    
    // System Configuration
    @FXML
    private void handleAddCoupon() {
        statusLabel.setText("Opening add coupon dialog...");
        // TODO: Open coupon dialog
    }
    
    @FXML
    private void handleSaveLoyaltySettings() {
        statusLabel.setText("Saving loyalty settings...");
        // TODO: Save loyalty settings
    }
    
    // Reports
    @FXML
    private void handleGenerateReport() {
        statusLabel.setText("Generating report...");
        // TODO: Generate report
    }
    
    @FXML
    private void handleExportReport() {
        statusLabel.setText("Exporting report...");
        // TODO: Export report to PDF
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Close current window
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.close();
            
            // Open login window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage loginStage = new Stage();
            loginStage.setTitle("Group12 GreenGrocer - Login");
            loginStage.setScene(new Scene(root, 960, 540));
            loginStage.centerOnScreen();
            loginStage.show();
            
            statusLabel.setText("Logged out successfully");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error logging out: " + e.getMessage());
        }
    }
}