package com.group12.greengrocer.controllers;

import com.group12.greengrocer.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for customer interface
 */
public class CustomerController {
    
    private User currentUser;
    
    @FXML
    private Label usernameLabel;
    
    /**
     * Initialize controller with user data
     */
    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Welcome, " + user.getUsername());
    }
    
    @FXML
    private void initialize() {
        // TODO: Load products from database
        // TODO: Setup shopping cart
        // TODO: Load user's order history
    }
    
    @FXML
    private void handleBrowseProducts() {
        // TODO: Display products by category
    }
    
    @FXML
    private void handleViewCart() {
        // TODO: Open shopping cart window
    }
    
    @FXML
    private void handleViewOrders() {
        // TODO: Display order history
    }
    
    @FXML
    private void handleViewDeliveries() {
        // TODO: Display delivery status
    }
    
    @FXML
    private void handleSendMessage() {
        // TODO: Open message to owner dialog
    }
    
    @FXML
    private void handleEditProfile() {
        // TODO: Open profile editing dialog
    }
    
    @FXML
    private void handleLogout() {
        // TODO: Close current window and return to login
    }
}