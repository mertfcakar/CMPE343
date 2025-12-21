package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer.fxml"));
            Parent root = loader.load();

            CustomerController controller = loader.getController();
            controller.initData(user);

            Stage stage = new Stage();
            stage.setTitle("Group12 GreenGrocer - Customer");
            stage.setScene(new Scene(root, 960, 540));
            stage.show();

            // Close login window
            ((Stage) usernameField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening customer interface");
        }
    }

    /**
     * Open carrier interface
     */
    private void openCarrierInterface(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/carrier.fxml"));
            Parent root = loader.load();

            CarrierController controller = loader.getController();
            controller.initData(user);

            Stage stage = new Stage();
            stage.setTitle("Group12 GreenGrocer - Carrier");
            stage.setScene(new Scene(root, 960, 540));
            stage.show();

            // Close login window
            ((Stage) usernameField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening carrier interface");
        }
    }

    /**
     * Open owner interface
     */
    private void openOwnerInterface(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/owner.fxml"));
            Parent root = loader.load();

            OwnerController controller = loader.getController();
            controller.initData(user);

            Stage stage = new Stage();
            stage.setTitle("Group12 GreenGrocer - Owner");
            stage.setScene(new Scene(root, 960, 540));
            stage.show();

            // Close login window
            ((Stage) usernameField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening owner interface");
        }
    }
}