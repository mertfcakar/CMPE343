package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller class for the Forgot Password screen.
 * This class handles user identity verification and password reset operations
 * by interacting with the {@link UserDAO}.
 * * @author Group12
 * @version 1.0
 */
public class ForgotPasswordController {

    /** Field for the user to enter their unique username. */
    @FXML
    private TextField usernameField;

    /** Field for the user to enter their registered email address. */
    @FXML
    private TextField emailField;

    /** Field for the user to enter their registered phone number. */
    @FXML
    private TextField phoneField;

    /** Field for the user to enter their new desired password. */
    @FXML
    private PasswordField newPasswordField;

    /** Label used to display error or success messages to the user. */
    @FXML
    private Label statusLabel;

    /**
     * Processes the password reset request.
     * Validates that all fields are filled and that the password meets the minimum length.
     * If the verification details match the database records, the password is updated.
     *
     * @param event The action event triggered by clicking the reset button.
     */
    @FXML
    private void handleResetPassword(ActionEvent event) {
        String user = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String newPass = newPasswordField.getText();

        // Validation: Check for empty fields
        if (user.isEmpty() || email.isEmpty() || phone.isEmpty() || newPass.isEmpty()) {
            statusLabel.setText("All fields are required!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Validation: Password length check
        if (newPass.length() < 4) {
            statusLabel.setText("Password too short (min 4 chars).");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Database operation: Attempt to reset password
        boolean success = UserDAO.resetPasswordSecure(user, email, phone, newPass);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("Password reset successfully! returning to login...");
            alert.showAndWait();
            switchToLogin(event);
        } else {
            statusLabel.setText("Verification Failed! Check details.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Redirects the user back to the login screen.
     * Loads the login FXML file and updates the current stage.
     *
     * @param event The action event triggered by the back button or successful reset.
     */
    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 960, 540));
            stage.setTitle("GreenGrocer - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}