package com.group12.greengrocer.controllers;

import java.security.SecureRandom;
import com.group12.greengrocer.database.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField hiddenPasswordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheck;
    
    @FXML private TextField addressField;
    @FXML private ComboBox<String> neighborhoodCombo;
    
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    
    @FXML private Label errorLabel;
    @FXML private Label strengthLabel;

    @FXML
    public void initialize() {
        neighborhoodCombo.getItems().addAll(
            "Beşiktaş", "Kadıköy", "Şişli", "Üsküdar", "Fatih", "Maltepe", "Bakırköy", "Sarıyer", "Beyoğlu"
        );
        
        // Şifre Binding
        visiblePasswordField.textProperty().bindBidirectional(hiddenPasswordField.textProperty());

        hiddenPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
            // Şifre yazılınca hata mesajını temizle
            resetFieldStyle(visiblePasswordField);
            resetFieldStyle(hiddenPasswordField);
        });

        // --- IPUCU (TOOLTIP) EKLEME ---
        emailField.setTooltip(new Tooltip("Örn: isim@ornek.com"));
        phoneField.setTooltip(new Tooltip("Başında 0 olmadan 10 hane. Örn: 5321234567"));

        // --- YAZARKEN HATALARI TEMİZLEME ---
        usernameField.setOnKeyTyped(e -> { errorLabel.setText(""); resetFieldStyle(usernameField); });
        emailField.setOnKeyTyped(e -> { errorLabel.setText(""); resetFieldStyle(emailField); });
        phoneField.setOnKeyTyped(e -> { errorLabel.setText(""); resetFieldStyle(phoneField); });
        addressField.setOnKeyTyped(e -> { errorLabel.setText(""); resetFieldStyle(addressField); });
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        // Önce tüm stilleri sıfırla
        resetAllStyles();
        errorLabel.setText("");

        String user = usernameField.getText().trim();
        String pass = hiddenPasswordField.getText();
        String addr = addressField.getText().trim();
        String hood = neighborhoodCombo.getValue();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        boolean hasError = false;
        StringBuilder errorMsg = new StringBuilder();

        // 1. Boş Alan Kontrolü
        if (user.isEmpty() || pass.isEmpty() || addr.isEmpty() || hood == null || email.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("⚠ Lütfen tüm alanları doldurunuz!");
            return; 
        }

        // 2. Kullanıcı Adı Kontrolü
        if (user.length() < 3) {
            setErrorStyle(usernameField);
            errorMsg.append("• Kullanıcı adı en az 3 karakter olmalı.\n");
            hasError = true;
        } else if (UserDAO.isUserExists(user)) {
            setErrorStyle(usernameField);
            errorMsg.append("• Bu kullanıcı adı zaten alınmış.\n");
            hasError = true;
        }

        // 3. Email Format Kontrolü
        // Regex: birşey@birşey.birşey
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            setErrorStyle(emailField);
            errorMsg.append("• Geçersiz Email! (Örn: user@mail.com)\n");
            hasError = true;
        }

        // 4. Telefon Format Kontrolü
        // Regex: Sadece rakam, 10 veya 11 hane
        if (!phone.matches("^\\d{10,11}$")) {
            setErrorStyle(phoneField);
            errorMsg.append("• Geçersiz Telefon! (Sadece rakam, 10-11 hane)\n");
            hasError = true;
        }

        // 5. Şifre Kontrolü
        if (!pass.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$")) {
            setErrorStyle(hiddenPasswordField);
            errorMsg.append("• Şifre Zayıf! En az 6 karakter, 1 harf ve 1 rakam içermeli.\n");
            hasError = true;
        }

        // Hata varsa göster ve çık
        if (hasError) {
            errorLabel.setText(errorMsg.toString());
            errorLabel.setTextFill(Color.RED);
            return;
        }

        // --- KAYIT İŞLEMİ ---
        if (UserDAO.registerCustomer(user, pass, addr, hood, email, phone)) {
            showInfo("Başarılı", "Kayıt tamamlandı!\nHoşgeldin, " + user);
            switchToLogin(event);
        } else {
            errorLabel.setText("⚠ Veritabanı hatası oluştu. Tekrar deneyin.");
        }
    }

    // --- YARDIMCI METOTLAR ---

    private void setErrorStyle(Control node) {
        // Kırmızı çerçeve ekle
        node.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 1px; -fx-border-radius: 5; -fx-background-radius: 5;");
    }

    private void resetFieldStyle(Control node) {
        // Normal stil (Mavi/Gri çerçeve yok veya varsayılan)
        node.setStyle("-fx-background-radius: 5;"); 
    }

    private void resetAllStyles() {
        resetFieldStyle(usernameField);
        resetFieldStyle(emailField);
        resetFieldStyle(phoneField);
        resetFieldStyle(addressField);
        resetFieldStyle(hiddenPasswordField);
        resetFieldStyle(visiblePasswordField);
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

    @FXML
    private void handleSuggestPassword() {
        String randomPass = generateRandomPass();
        hiddenPasswordField.setText(randomPass);
        showPasswordCheck.setSelected(true);
        handleTogglePassword();
    }

    @FXML
    private void handleCopyPassword() {
        String pass = hiddenPasswordField.getText();
        if (!pass.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(pass);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    private String generateRandomPass() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private void updatePasswordStrength(String password) {
        int len = password.length();
        if (len == 0) strengthLabel.setText("");
        else if (len < 4) { strengthLabel.setText("Weak"); strengthLabel.setTextFill(Color.RED); }
        else if (len < 8) { strengthLabel.setText("Medium"); strengthLabel.setTextFill(Color.ORANGE); }
        else { strengthLabel.setText("Strong ✅"); strengthLabel.setTextFill(Color.GREEN); }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.RED);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}