package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField passwordTextField; // Görünür şifre için
    @FXML
    public CheckBox showPasswordCheck;
    @FXML
    public Label messageLabel;
    @FXML
    public Button loginButton;

    private int loginAttempts = 0;
    private final int MAX_ATTEMPTS = 3;
    private int remainingSeconds;

    @FXML
    public void initialize() {
        // Null kontrolü ile güvenli hale getirdik
        if (passwordField != null) {
            passwordField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER)
                    handleLogin();
            });
        }

        // Hata veren kısım burasıydı, artık null olsa bile uygulama çökmez
        if (passwordTextField != null) {
            passwordTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER)
                    handleLogin();
            });
        }
    }

    @FXML
    public void handleLogin() {
        String u = usernameField.getText().trim();
        // Şifre kutusu veya görünür kutu hangisi aktifse onu al
        String p = showPasswordCheck.isSelected() ? passwordTextField.getText() : passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Username and Password cannot be empty!");
            return;
        }

        User user = UserDAO.login(u, p);

        if (user != null) {
            openDashboard(user);
        } else {
            loginAttempts++;
            if (loginAttempts >= MAX_ATTEMPTS) {
                startLockout();
            } else {
                showError("Invalid credentials! Attempt: " + loginAttempts + "/" + MAX_ATTEMPTS);
            }
        }
    }

    private void openDashboard(User user) {
        try {
            FXMLLoader loader;
            Parent root;
            String title = "";

            if ("carrier".equalsIgnoreCase(user.getRole())) {
                loader = new FXMLLoader(getClass().getResource("/fxml/carrier.fxml"));
                root = loader.load();
                CarrierController controller = loader.getController();
                controller.initData(user);
                title = "Carrier Panel - " + user.getUsername();

            } else if ("owner".equalsIgnoreCase(user.getRole())) {
                loader = new FXMLLoader(getClass().getResource("/fxml/owner.fxml"));
                root = loader.load();
                OwnerController controller = loader.getController();
                controller.initData(user);
                title = "Owner Dashboard - " + user.getUsername();

            } else {
                loader = new FXMLLoader(getClass().getResource("/fxml/customer.fxml"));
                root = loader.load();
                CustomerController controller = loader.getController();
                controller.initData(user);
                title = "GreenGrocer Market - " + user.getUsername();
            }

            // Mevcut pencerede sahneyi değiştir
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

    // --- TEK PENCERE GEÇİŞLERİ ---

    @FXML
    public void switchToRegister(ActionEvent event) {
        changeScene(event, "/fxml/register.fxml", "GreenGrocer - Register");
    }

    @FXML
    public void switchToForgotPassword(ActionEvent event) {
        changeScene(event, "/fxml/forgot_password.fxml", "GreenGrocer - Reset Password");
    }

    private void changeScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Ekran boyutunu korumak için (960x540)
            stage.setScene(new Scene(root, 960, 540));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading screen: " + fxmlPath);
        }
    }

    // --- YARDIMCI FONKSİYONLAR ---

    private void startLockout() {
        loginButton.setDisable(true);
        usernameField.setDisable(true);
        passwordField.setDisable(true);
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
                loginAttempts = 0;
                messageLabel.setText("System ready. Please try again.");
                messageLabel.setTextFill(Color.GREEN);
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    @FXML
    public void handlePasswordVisibility() {
        if (showPasswordCheck.isSelected()) {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
    }

    private void showError(String m) {
        messageLabel.setText(m);
        messageLabel.setTextFill(Color.RED);
    }
}