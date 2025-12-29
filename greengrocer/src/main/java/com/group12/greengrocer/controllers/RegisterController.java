package com.group12.greengrocer.controllers;

import java.security.SecureRandom;
import com.group12.greengrocer.database.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField hiddenPasswordField; // Gizli Şifre
    @FXML private TextField visiblePasswordField;    // Görünür Şifre
    @FXML private CheckBox showPasswordCheck;
    
    @FXML private TextField addressField;
    @FXML private ComboBox<String> neighborhoodCombo;
    @FXML private TextField contactField;
    @FXML private Label errorLabel;
    @FXML private Label strengthLabel;

    @FXML
    public void initialize() {
        neighborhoodCombo.getItems().addAll(
            "Beşiktaş", "Kadıköy", "Şişli", "Üsküdar", "Fatih", "Maltepe", "Bakırköy", "Sarıyer", "Beyoğlu"
        );
        
        usernameField.setOnKeyTyped(e -> errorLabel.setText(""));
        addressField.setOnKeyTyped(e -> errorLabel.setText(""));

        // --- BINDING: İki kutunun metnini birbirine bağla ---
        visiblePasswordField.textProperty().bindBidirectional(hiddenPasswordField.textProperty());

        // Şifre Gücü Dinleyicisi
        hiddenPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
            errorLabel.setText("");
        });
    }

    // --- ŞİFRE GÖSTER / GİZLE ---
    @FXML
    private void handleTogglePassword() {
        if (showPasswordCheck.isSelected()) {
            visiblePasswordField.setVisible(true); visiblePasswordField.setManaged(true);
            hiddenPasswordField.setVisible(false); hiddenPasswordField.setManaged(false);
        } else {
            hiddenPasswordField.setVisible(true); hiddenPasswordField.setManaged(true);
            visiblePasswordField.setVisible(false); visiblePasswordField.setManaged(false);
        }
    }

    private void updatePasswordStrength(String password) {
        int len = password.length();
        if (len == 0) {
            strengthLabel.setText("");
        } else if (len < 4) {
            strengthLabel.setText("Strength: Too Short (Weak)");
            strengthLabel.setTextFill(Color.RED);
        } else if (len < 8) {
            strengthLabel.setText("Strength: Medium");
            strengthLabel.setTextFill(Color.ORANGE);
        } else {
            strengthLabel.setText("Strength: Strong ✅");
            strengthLabel.setTextFill(Color.GREEN);
        }
    }

    @FXML
    private void handleSuggestPassword() {
        String randomPass = generateRandomPass();
        hiddenPasswordField.setText(randomPass);
        
        // Kullanıcı generate yapınca şifreyi otomatik göster
        showPasswordCheck.setSelected(true);
        handleTogglePassword(); 
    }

    @FXML
    private void handleCopyPassword() {
        String pass = hiddenPasswordField.getText();
        if (pass.isEmpty()) return;

        ClipboardContent content = new ClipboardContent();
        content.putString(pass);
        Clipboard.getSystemClipboard().setContent(content);

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Password Copied");
        alert.setHeaderText("Important Reminder!");
        alert.setContentText("Password copied to clipboard.\n\nPLEASE NOTE IT DOWN IMMEDIATELY!\nIf you lose it, you may lose access.");
        alert.showAndWait();
    }

    private String generateRandomPass() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @FXML
    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = hiddenPasswordField.getText(); // Binding olduğu için günceldir
        String addr = addressField.getText().trim();
        String hood = neighborhoodCombo.getValue();
        String contact = contactField.getText().trim();

        // 1. Boş Alanlar
        if (user.isEmpty() || pass.isEmpty() || addr.isEmpty() || hood == null || contact.isEmpty()) {
            showError("⚠ Please fill in all fields!");
            return;
        }

        // 2. Kullanıcı Adı
        if (user.length() < 3) {
            showError("⚠ Username too short (min 3 chars).");
            return;
        }
        if (UserDAO.isUserExists(user)) {
            showError("⚠ Username '" + user + "' is already taken.");
            return;
        }

        // 3. Şifre (Min 4 Karakter)
        if (pass.length() < 4) {
            showError("⚠ Password too weak! Use at least 4 chars.");
            return;
        }

        // 4. İletişim (Email veya Telefon Regex)
        boolean isEmail = contact.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        boolean isPhone = contact.matches("^\\+?[0-9 ]{10,15}$");
        
        if (!isEmail && !isPhone) {
            showError("⚠ Invalid Contact! Enter Email or Phone.");
            return;
        }

        // 5. Adres (Min 10 karakter + boşluk)
        if (addr.length() < 10 || !addr.contains(" ")) {
            showError("⚠ Address too short. Please enter full address.");
            return;
        }

        // Kayıt
        if (UserDAO.registerCustomer(user, pass, addr, hood, contact)) {
            showInfo("Success!", "Registration Completed.\nWelcome, " + user + "!");
            ((Stage) usernameField.getScene().getWindow()).close();
        } else {
            showError("⚠ Database error occurred.");
        }
    }

    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: #d32f2f;");
        errorLabel.setText(message);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}