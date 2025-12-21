package com.group12.greengrocer.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;

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
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // TODO: Implement authentication logic
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password");
        } else {
            messageLabel.setText("Login functionality coming soon...");
        }
    }
    
    @FXML
    private void handleRegister() {
        // TODO: Implement registration logic
        messageLabel.setText("Registration functionality coming soon...");
    }
}