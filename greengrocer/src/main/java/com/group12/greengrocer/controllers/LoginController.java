package com.group12.greengrocer.controllers;

import java.security.SecureRandom;
import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.User;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
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
    private final int MAX_ATTEMPTS = 3; 
    private int remainingSeconds;

    @FXML
    public void initialize() {
        passwordField.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ENTER) handleLogin(); });
        passwordTextField.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ENTER) handleLogin(); });
    }

    @FXML
    public void handleLogin() {
        String u = usernameField.getText().trim();
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

    @FXML
    public void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Customer Registration");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open registration screen.");
        }
    }

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
            passwordTextField.setVisible(true); passwordTextField.setManaged(true);
            passwordField.setVisible(false); passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true); passwordField.setManaged(true);
            passwordTextField.setVisible(false); passwordTextField.setManaged(false);
        }
    }

    @FXML
    public void handleChangePassword() {
        Stage stage = new Stage();
        stage.setTitle("Security Wizard");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2;");

        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 10; -fx-background-radius: 5;");
        Label lblRuleTitle = new Label("Security Check:");
        lblRuleTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        Label lblRuleDesc = new Label("• Enter your registered contact info to verify identity.");
        infoBox.getChildren().addAll(lblRuleTitle, lblRuleDesc);

        Label lblSuggest = new Label("Strong Suggestion:");
        TextField txtSuggest = new TextField(generateRandomPass());
        txtSuggest.setEditable(false);
        Button btnSuggest = new Button("New");
        Button btnCopy = new Button("Copy");
        
        btnSuggest.setOnAction(e -> txtSuggest.setText(generateRandomPass()));
        btnCopy.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(txtSuggest.getText());
            Clipboard.getSystemClipboard().setContent(content);
            btnCopy.setText("OK");
        });
        HBox suggestBox = new HBox(5, txtSuggest, btnSuggest, btnCopy);

        TextField txtUser = new TextField(); txtUser.setPromptText("Username");
        TextField txtContact = new TextField(); txtContact.setPromptText("Registered Email or Phone"); // GÜVENLİK ALANI
        PasswordField txtNewPass = new PasswordField(); txtNewPass.setPromptText("New Password");
        PasswordField txtConfirmPass = new PasswordField(); txtConfirmPass.setPromptText("Confirm Password");

        Label lblStatus = new Label("● Min 3 Chars Required");
        lblStatus.setTextFill(Color.RED);

        txtNewPass.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() >= 3) {
                lblStatus.setText("● Password Strength: OK");
                lblStatus.setTextFill(Color.GREEN);
            } else {
                lblStatus.setText("● Min 3 Chars Required");
                lblStatus.setTextFill(Color.RED);
            }
        });

        Button btnUpdate = new Button("Verify & Update Password");
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnUpdate.setPrefHeight(35);

        btnUpdate.setOnAction(e -> {
            String u = txtUser.getText().trim();
            String contact = txtContact.getText().trim();
            String p1 = txtNewPass.getText();
            String p2 = txtConfirmPass.getText();

            if (u.isEmpty() || contact.isEmpty() || p1.isEmpty()) { showAlert("Error", "All fields are required!"); return; }
            if (p1.length() < 3) { showAlert("Error", "Password too short!"); return; }
            if (!p1.equals(p2)) { showAlert("Error", "Passwords do not match!"); return; }

            // GÜVENLİ GÜNCELLEME
            if (UserDAO.updatePasswordSecure(u, contact, p1)) {
                showAlert("Success", "Identity verified & Password updated!");
                stage.close();
            } else {
                showAlert("Security Error", "Username or Contact Info mismatch!");
            }
        });

        root.getChildren().addAll(infoBox, new Separator(), lblSuggest, suggestBox, new Separator(),
                                  new Label("Username:"), txtUser,
                                  new Label("Security Check (Email/Phone):"), txtContact,
                                  new Label("New Password:"), txtNewPass, lblStatus,
                                  new Label("Confirm:"), txtConfirmPass, new Separator(), btnUpdate);

        stage.setScene(new Scene(root, 400, 600));
        stage.show();
    }

    private String generateRandomPass() {
        String c = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(c.charAt(r.nextInt(c.length())));
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