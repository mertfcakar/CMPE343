package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * Controller for login screen
 */
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private void handleLogin() {
        // Clear previous message
        messageLabel.setText("");
        
        // Get input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }
        
        // Attempt login
        User user = UserDAO.login(username, password);
        
        if (user != null) {
            // Login successful
            showSuccess("Login successful! Welcome " + user.getUsername());
            
            // TODO: Open appropriate interface based on role
            switch (user.getRole()) {
                case "customer":
                    openCustomerInterface(user);
                    break;
                case "carrier":
                    openCarrierInterface(user);
                    break;
                case "owner":
                    openOwnerInterface(user);
                    break;
                default:
                    showError("Unknown role: " + user.getRole());
            }
            
        } else {
            // Login failed
            showError("Invalid username or password!");
        }
    }
    
    @FXML
    private void handleRegister() {
        messageLabel.setText("Registration functionality coming soon...");
        messageLabel.setTextFill(Color.BLUE);
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.RED);
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(Color.GREEN);
    }
    
    /**
     * Open customer interface
     */
    private void openCustomerInterface(User user) {
        System.out.println("Opening customer interface for: " + user.getUsername());
        // TODO: Implement customer interface
    }
    
    /**
     * Open carrier interface
     */
    private void openCarrierInterface(User user) {
        System.out.println("Opening carrier interface for: " + user.getUsername());
        // TODO: Implement carrier interface
    }
    
    /**
     * Open owner interface
     */
    private void openOwnerInterface(User user) {
        System.out.println("Opening owner interface for: " + user.getUsername());
        // TODO: Implement owner interface
    }
}