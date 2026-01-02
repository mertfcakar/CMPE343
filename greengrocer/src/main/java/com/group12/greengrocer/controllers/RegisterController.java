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

/**
 * Controller class for the User Registration screen.
 * Handles the logic for new customer sign-ups, including input validation,
 * password strength checking, secure password generation, and database insertion.
 *
 * @author Group12
 * @version 1.0
 */
public class RegisterController {

    // UI Components for User Input
    @FXML private TextField usernameField;
    @FXML private PasswordField hiddenPasswordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheck;
    
    @FXML private TextField addressField;
    @FXML private ComboBox<String> neighborhoodCombo;
    
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    
    // UI Components for Feedback
    @FXML private Label errorLabel;
    @FXML private Label strengthLabel;

    /**
     * Initializes the controller class.
     * Sets up initial data (neighborhoods), binds password fields for visibility toggling,
     * adds listeners for real-time password strength updates, and configures validation tooltips.
     */
    @FXML
    public void initialize() {
        neighborhoodCombo.getItems().addAll(
            "Beşiktaş", "Kadıköy", "Şişli", "Üsküdar", "Fatih", "Maltepe", "Bakırköy", "Sarıyer", "Beyoğlu"
        );
        
        // Password Binding: Synchronize text between hidden and visible fields
        visiblePasswordField.textProperty().bindBidirectional(hiddenPasswordField.textProperty());

        hiddenPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
            // Clear error style when user starts typing
            resetFieldStyle(visiblePasswordField);
            resetFieldStyle(hiddenPasswordField);
        });

        // --- TOOLTIPS ---
        emailField.setTooltip(new Tooltip("Ex: name@example.com"));
        phoneField.setTooltip(new Tooltip("10 digits without leading 0. Ex: 5321234567"));

        // --- CLEAR ERRORS ON TYPING ---
        usernameField.setOnKeyTyped(e -> { errorLabel.setText(""); resetFieldStyle(usernameField); });
        emailField.setOnKeyTyped(e -> { errorLabel.setText(""); resetFieldStyle(emailField); });
        phoneField.setOnKeyTyped(e -> { errorLabel.setText(""); resetFieldStyle(phoneField); });
        addressField.setOnKeyTyped(e -> { errorLabel.setText(""); resetFieldStyle(addressField); });
    }

    /**
     * Handles the registration process when the register button is clicked.
     * Performs a series of validations:
     * 1. Checks for empty fields.
     * 2. Validates username uniqueness and length.
     * 3. Validates email format via Regex.
     * 4. Validates phone number format via Regex.
     * 5. Enforces password complexity requirements.
     * * If all checks pass, creates a new user via {@link UserDAO}.
     *
     * @param event The action event triggered by the register button.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // Reset styles before validation
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

        // 1. Empty Field Check
        if (user.isEmpty() || pass.isEmpty() || addr.isEmpty() || hood == null || email.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("⚠ Please fill in all fields!");
            return; 
        }

        // 2. Username Validation
        if (user.length() < 3) {
            setErrorStyle(usernameField);
            errorMsg.append("• Username must be at least 3 chars.\n");
            hasError = true;
        } else if (UserDAO.isUserExists(user)) {
            setErrorStyle(usernameField);
            errorMsg.append("• Username is already taken.\n");
            hasError = true;
        }

        // 3. Email Format Validation
        // Regex: something@something.something
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            setErrorStyle(emailField);
            errorMsg.append("• Invalid Email! (Ex: user@mail.com)\n");
            hasError = true;
        }

        // 4. Phone Format Validation
        // Regex: Only digits, 10 or 11 digits
        if (!phone.matches("^\\d{10,11}$")) {
            setErrorStyle(phoneField);
            errorMsg.append("• Invalid Phone! (Digits only, 10-11 chars)\n");
            hasError = true;
        }

        // 5. Password Complexity Validation
        // Regex: Min 6 chars, at least 1 letter and 1 number
        if (!pass.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$")) {
            setErrorStyle(hiddenPasswordField);
            errorMsg.append("• Weak Password! Min 6 chars, 1 letter & 1 number.\n");
            hasError = true;
        }

        // Display accumulated errors
        if (hasError) {
            errorLabel.setText(errorMsg.toString());
            errorLabel.setTextFill(Color.RED);
            return;
        }

        // --- REGISTRATION EXECUTION ---
        if (UserDAO.registerCustomer(user, pass, addr, hood, email, phone)) {
            showInfo("Success", "Registration Complete!\nWelcome, " + user);
            switchToLogin(event);
        } else {
            errorLabel.setText("⚠ Database error occurred. Please try again.");
        }
    }

    // --- HELPER METHODS ---

    /**
     * Applies a visual error style (red border) to the specified control.
     * @param node The UI control to highlight.
     */
    private void setErrorStyle(Control node) {
        node.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 1px; -fx-border-radius: 5; -fx-background-radius: 5;");
    }

    /**
     * Resets the visual style of the specified control to default.
     * @param node The UI control to reset.
     */
    private void resetFieldStyle(Control node) {
        node.setStyle("-fx-background-radius: 5;"); 
    }

    /**
     * Resets styles for all input fields in the form.
     */
    private void resetAllStyles() {
        resetFieldStyle(usernameField);
        resetFieldStyle(emailField);
        resetFieldStyle(phoneField);
        resetFieldStyle(addressField);
        resetFieldStyle(hiddenPasswordField);
        resetFieldStyle(visiblePasswordField);
    }

    /**
     * Navigates the user back to the Login screen.
     * @param event The action event triggering the navigation.
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

    /**
     * Toggles the visibility of the password field.
     * Switches between PasswordField (masked) and TextField (visible).
     */
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

    /**
     * Generates a random secure password and populates the password field.
     * Automatically shows the password so the user can see/copy it.
     */
    @FXML
    private void handleSuggestPassword() {
        String randomPass = generateRandomPass();
        hiddenPasswordField.setText(randomPass);
        showPasswordCheck.setSelected(true);
        handleTogglePassword();
    }

    /**
     * Copies the current content of the password field to the system clipboard.
     */
    @FXML
    private void handleCopyPassword() {
        String pass = hiddenPasswordField.getText();
        if (!pass.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(pass);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    /**
     * Generates a random alphanumeric string using SecureRandom.
     * @return A random 12-character string including special characters.
     */
    private String generateRandomPass() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    /**
     * Updates the strength label based on the length of the password.
     * @param password The current password text.
     */
    private void updatePasswordStrength(String password) {
        int len = password.length();
        if (len == 0) strengthLabel.setText("");
        else if (len < 4) { strengthLabel.setText("Weak"); strengthLabel.setTextFill(Color.RED); }
        else if (len < 8) { strengthLabel.setText("Medium"); strengthLabel.setTextFill(Color.ORANGE); }
        else { strengthLabel.setText("Strong ✅"); strengthLabel.setTextFill(Color.GREEN); }
    }

    /**
     * Displays an error message to the user via the UI label.
     * @param message The error text to display.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.RED);
    }

    /**
     * Displays an informational alert dialog.
     * @param title The title of the alert window.
     * @param content The message content of the alert.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}