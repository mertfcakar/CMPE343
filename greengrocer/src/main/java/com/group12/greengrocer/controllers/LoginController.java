package com.group12.greengrocer.controllers;

import java.security.SecureRandom;
import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.User;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public TextField passwordTextField;
    @FXML public CheckBox showPasswordCheck;
    @FXML public Label messageLabel;
    @FXML public Button loginButton;

    private int loginAttempts = 0;
    private final int MAX_ATTEMPTS = 3; // 3 Hatalı denemede kilitlenir
    private int remainingSeconds;

    @FXML
    public void initialize() {
        // Enter tuşuna basınca giriş yapmayı sağla
        passwordField.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ENTER) handleLogin(); });
        passwordTextField.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ENTER) handleLogin(); });
    }

    // --- GİRİŞ İŞLEMİ (LOGIN) ---
    @FXML
    public void handleLogin() {
        String u = usernameField.getText().trim();
        String p = showPasswordCheck.isSelected() ? passwordTextField.getText() : passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Username and Password cannot be empty!");
            return;
        }

        // Veritabanından kullanıcıyı sorgula
        User user = UserDAO.login(u, p);

        if (user != null) {
            // Giriş Başarılı -> Rolüne göre ekran aç
            openDashboard(user);
        } else {
            // Giriş Başarısız -> Sayaç artır
            loginAttempts++;
            if (loginAttempts >= MAX_ATTEMPTS) {
                startLockout(); 
            } else {
                showError("Invalid credentials! Attempt: " + loginAttempts + "/" + MAX_ATTEMPTS);
            }
        }
    }

    // --- ROL YÖNETİMİ VE EKRAN GEÇİŞİ ---
    private void openDashboard(User user) {
        try {
            FXMLLoader loader;
            Parent root;
            String title = "";

            if ("carrier".equalsIgnoreCase(user.getRole())) {
                loader = new FXMLLoader(getClass().getResource("/fxml/carrier.fxml"));
                root = loader.load();
                
                // Kurye Controller'a veriyi gönder
                CarrierController controller = loader.getController();
                controller.initData(user);
                
                title = "Carrier Panel - " + user.getUsername();

            } else if ("owner".equalsIgnoreCase(user.getRole())) {
                loader = new FXMLLoader(getClass().getResource("/fxml/owner.fxml"));
                root = loader.load();
                
                // Owner Controller'a veriyi gönder
                OwnerController controller = loader.getController();
                controller.initData(user);
                
                title = "Owner Dashboard - " + user.getUsername();

            } else { // Customer (Varsayılan)
                loader = new FXMLLoader(getClass().getResource("/fxml/customer.fxml"));
                root = loader.load();
                
                // Customer Controller'a veriyi gönder
                CustomerController controller = loader.getController();
                controller.initData(user);
                
                title = "GreenGrocer Market - " + user.getUsername();
            }

            // Mevcut pencereyi değiştir
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("System Error: Could not load the dashboard.");
        }
    }

    // --- KAYIT OL (REGISTER) ---
    @FXML
    public void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("New Customer Registration");
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open registration screen.\nMake sure register.fxml exists.");
        }
    }

    // --- SİSTEM KİLİTLEME (GÜVENLİK) ---
    private void startLockout() {
        loginButton.setDisable(true);
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        passwordTextField.setDisable(true);
        remainingSeconds = 30; // 30 Saniye kilitli kal

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            messageLabel.setText("SYSTEM LOCKED! Wait: " + remainingSeconds + "s");
            messageLabel.setTextFill(Color.DARKRED);
            
            if (remainingSeconds <= 0) {
                timeline.stop();
                loginButton.setDisable(false);
                usernameField.setDisable(false);
                passwordField.setDisable(false);
                passwordTextField.setDisable(false);
                loginAttempts = 0;
                messageLabel.setText("System ready. Please try again.");
                messageLabel.setTextFill(Color.GREEN);
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    // --- ŞİFRE GÖSTER / GİZLE ---
    @FXML
    public void handlePasswordVisibility() {
        if (showPasswordCheck.isSelected()) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true); passwordTextField.setManaged(true);
            passwordField.setVisible(false); passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true); passwordField.setManaged(true);
            passwordTextField.setVisible(false); passwordTextField.setManaged(false);
        }
    }

    // --- SECURITY WIZARD (ŞİFRE DEĞİŞTİRME) ---
    @FXML
    public void handleChangePassword() {
        Stage stage = new Stage();
        stage.setTitle("Security Wizard");
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff; -fx-border-color: #2e7d32; -fx-border-width: 2;");

        Label guideLabel = new Label("Password Rule:\n• Minimum 3 characters required.");
        guideLabel.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-text-fill: #1b5e20; -fx-font-weight: bold;");
        guideLabel.setMaxWidth(Double.MAX_VALUE);

        // Şifre Önerici
        Label suggestLabel = new Label("Strong Suggestion:");
        TextField suggestField = new TextField(generateRandomPass());
        suggestField.setEditable(false);
        Button suggestNewBtn = new Button("Suggest New One");
        suggestNewBtn.setOnAction(e -> suggestField.setText(generateRandomPass()));
        Button copyBtn = new Button("Copy");
        copyBtn.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(suggestField.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });
        HBox suggestBox = new HBox(5, suggestField, suggestNewBtn, copyBtn);

        TextField userIn = new TextField(); userIn.setPromptText("Username");
        PasswordField pass1 = new PasswordField(); pass1.setPromptText("New Password");
        PasswordField pass2 = new PasswordField(); pass2.setPromptText("Confirm New Password");

        Label ruleLen = new Label("● Minimum 3 Characters Status"); 
        ruleLen.setTextFill(Color.RED);

        pass1.textProperty().addListener((obs, old, val) -> {
            ruleLen.setTextFill(val.length() >= 3 ? Color.GREEN : Color.RED);
        });

        Button updateBtn = new Button("Update Password");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");

        updateBtn.setOnAction(e -> {
            String u = userIn.getText().trim();
            String p1 = pass1.getText();
            String p2 = pass2.getText();

            if (u.isEmpty() || p1.isEmpty()) { showAlert("Error", "Fields required."); return; }
            if (p1.length() < 3) { showAlert("Error", "Min 3 chars required!"); return; }
            if (!p1.equals(p2)) { showAlert("Error", "Passwords do not match!"); return; }

            String oldPass = UserDAO.getUserPassword(u);
            if (oldPass != null && oldPass.equals(p1)) {
                showAlert("Security", "Cannot use your old password.");
                return;
            }

            if (UserDAO.updatePassword(u, p1)) {
                showAlert("Success", "Password updated!");
                stage.close();
            } else {
                showAlert("Error", "User not found.");
            }
        });

        root.getChildren().addAll(guideLabel, new Separator(), suggestLabel, suggestBox, new Separator(),
                                  new Label("Username:"), userIn, new Label("New Password:"), pass1, 
                                  ruleLen, new Label("Confirm New Password:"), pass2, 
                                  updateBtn);

        stage.setScene(new Scene(root, 400, 550));
        stage.show();
    }

    // --- YARDIMCI METODLAR ---
    private String generateRandomPass() {
        String c = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(c.charAt(r.nextInt(c.length())));
        return sb.toString();
    }

    private void showError(String m) { 
        messageLabel.setText(m); 
        messageLabel.setTextFill(Color.RED); 
    }
    
    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); 
        a.setTitle(t); 
        a.setHeaderText(null); 
        a.setContentText(c); 
        a.showAndWait();
    }
}