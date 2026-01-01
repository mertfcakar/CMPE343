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

public class ForgotPasswordController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Label statusLabel;

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String user = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String newPass = newPasswordField.getText();

        if (user.isEmpty() || email.isEmpty() || phone.isEmpty() || newPass.isEmpty()) {
            statusLabel.setText("All fields are required!");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (newPass.length() < 4) {
            statusLabel.setText("Password too short (min 4 chars).");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Veritabanı Kontrolü ve Güncelleme
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