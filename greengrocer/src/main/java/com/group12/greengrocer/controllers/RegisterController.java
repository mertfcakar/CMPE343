package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> neighborhoodCombo;
    @FXML private TextField contactField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        neighborhoodCombo.getItems().addAll(
            "Beşiktaş", "Kadıköy", "Şişli", "Üsküdar", "Fatih", "Maltepe", "Bakırköy"
        );
    }

    @FXML
    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        String addr = addressField.getText();
        String hood = neighborhoodCombo.getValue();
        String contact = contactField.getText();

        if (user.isEmpty() || pass.isEmpty() || addr.isEmpty() || hood == null || contact.isEmpty()) {
            errorLabel.setText("All fields are required!");
            return;
        }

        // Benzersiz Kullanıcı Adı Kontrolü
        if (UserDAO.isUserExists(user)) {
            errorLabel.setText("Username already taken!");
            return;
        }

        // Basit Şifre Kontrolü (Hocanın istediği "validation")
        if (pass.length() < 3) {
            errorLabel.setText("Password too short (min 3 chars)!");
            return;
        }

        // Kayıt İşlemi
        if (UserDAO.registerCustomer(user, pass, addr, hood, contact)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Registration Successful! Please login.");
            alert.showAndWait();

            // Pencereyi kapat
            ((Stage) usernameField.getScene().getWindow()).close();
        } else {
            errorLabel.setText("Database error occurred.");
        }
    }
}