package com.group12.greengrocer.controllers;

import java.security.SecureRandom;

import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.User;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // EKLENDİ
import javafx.geometry.Insets;
import javafx.scene.Parent; // EKLENDİ
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

    // [KRİTİK]: CarrierController'ın kullanıcı ID'sine erişmesi için gereken statik değişken
    public static User loggedInUser; 

    private int loginAttempts = 0;
    private final int MAX_ATTEMPTS = 3;
    private int remainingSeconds;

    @FXML
    public void initialize() {
        // [CHECKLIST: Enter Tuşu Desteği]
        passwordField.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ENTER) handleLogin(); });
        passwordTextField.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ENTER) handleLogin(); });
    }

    @FXML
    public void handleLogin() {
        // [CHECKLIST: Boş Alan Kontrolü]
        String u = usernameField.getText().trim();
        String p = showPasswordCheck.isSelected() ? passwordTextField.getText() : passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Fields cannot be empty!");
            return;
        }

        User user = UserDAO.login(u, p);

        if (user != null) {
            loggedInUser = user; // Giriş yapan kullanıcıyı kaydet
            loginAttempts = 0;
            messageLabel.setText("Login successful!");
            messageLabel.setTextFill(Color.GREEN);

            // [GÜNCELLEME: EKRAN GEÇİŞİ]
            try {
                // Dosya ismini carrier.fxml olarak güncelledik
                java.net.URL fxmlUrl = getClass().getResource("/fxml/carrier.fxml");
                
                if (fxmlUrl == null) {
                    showError("HATA: /fxml/carrier.fxml bulunamadı!");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                // Kurye ekranı için geniş bir sahne boyutu ayarladık
                stage.setScene(new Scene(root, 1000, 600)); 
                stage.setTitle("GreenGrocer Carrier Portal - " + user.getUsername());
                stage.centerOnScreen();
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                showError("FXML Load Error: " + e.getMessage());
            }

        } else {
            loginAttempts++;
            if (loginAttempts >= MAX_ATTEMPTS) {
                startLockout(); 
            } else {
                // [CHECKLIST: Akıllı Uyarı Sistemi]
                String warning = (loginAttempts == 2) 
                    ? "LAST CHANCE! Next fail will lock system!" 
                    : "Invalid login! Attempt: " + loginAttempts + "/" + MAX_ATTEMPTS;
                showError(warning);
            }
        }
    }

    // [CHECKLIST: Tam Kilitleme & Canlı Geri Sayım]
    private void startLockout() {
        loginButton.setDisable(true);
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        passwordTextField.setDisable(true);
        showPasswordCheck.setDisable(true);
        remainingSeconds = 30;

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
                showPasswordCheck.setDisable(false);
                loginAttempts = 0;
                messageLabel.setText("System ready.");
                messageLabel.setTextFill(Color.GREEN);
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @FXML
    public void handleChangePassword() {
        Stage stage = new Stage();
        stage.setTitle("Security Wizard");
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff; -fx-border-color: #2e7d32; -fx-border-width: 2;");

        // [CHECKLIST: Rehber Kutusu]
        Label guideLabel = new Label("Password Rule:\n• Minimum 3 characters required.");
        guideLabel.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-text-fill: #1b5e20; -fx-font-weight: bold;");
        guideLabel.setMaxWidth(Double.MAX_VALUE);

        // [CHECKLIST: Şifre Önerici & Kopyalama]
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
        TextField pass1Visible = new TextField(); pass1Visible.setManaged(false); pass1Visible.setVisible(false);
        pass1Visible.textProperty().bindBidirectional(pass1.textProperty());

        Label evaluationLabel = new Label("Evaluation: Waiting...");
        evaluationLabel.setStyle("-fx-font-weight: bold;");

        Label ruleLen = new Label("● Minimum 3 Characters Status"); 
        ruleLen.setTextFill(Color.RED);

        pass1.textProperty().addListener((obs, old, val) -> {
            updateStrengthLabel(val, evaluationLabel);
            ruleLen.setTextFill(val.length() >= 3 ? Color.GREEN : Color.RED);
        });

        PasswordField pass2 = new PasswordField(); pass2.setPromptText("Confirm New Password");
        TextField pass2Visible = new TextField(); pass2Visible.setManaged(false); pass2Visible.setVisible(false);
        pass2Visible.textProperty().bindBidirectional(pass2.textProperty());

        CheckBox innerToggle = new CheckBox("Show Passwords");
        innerToggle.setOnAction(e -> {
            boolean show = innerToggle.isSelected();
            pass1Visible.setVisible(show); pass1Visible.setManaged(show);
            pass1.setVisible(!show); pass1.setManaged(!show);
            pass2Visible.setVisible(show); pass2Visible.setManaged(show);
            pass2.setVisible(!show); pass2.setManaged(!show);
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
                                  new Label("Username:"), userIn, new Label("New Password:"), pass1, pass1Visible,
                                  evaluationLabel, ruleLen, new Label("Confirm New Password:"), pass2, pass2Visible,
                                  innerToggle, updateBtn);

        stage.setScene(new Scene(root, 480, 750));
        stage.show();
    }

    private void updateStrengthLabel(String v, Label l) {
        if (v.isEmpty()) { l.setText("Evaluation: Waiting..."); l.setTextFill(Color.GRAY); }
        else if (v.length() < 3) { l.setText("Evaluation: Too Short ❌"); l.setTextFill(Color.RED); }
        else if (v.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$")) {
            l.setText("Evaluation: EXCELLENT ✅"); l.setTextFill(Color.GREEN);
        } else if (v.matches(".*[!@#$%^&*()].*") && v.matches(".*[0-9].*")) {
            l.setText("Evaluation: GOOD ⚠️"); l.setTextFill(Color.ORANGE);
        } else {
            l.setText("Evaluation: WEAK (Try adding special chars) ❌"); l.setTextFill(Color.BROWN);
        }
    }

    private String generateRandomPass() {
        String c = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(c.charAt(r.nextInt(c.length())));
        return sb.toString();
    }

    private void showError(String m) { messageLabel.setText(m); messageLabel.setTextFill(Color.RED); }
    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }

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

    @FXML public void handleForgotPassword() { showError("Contact admin."); }
}