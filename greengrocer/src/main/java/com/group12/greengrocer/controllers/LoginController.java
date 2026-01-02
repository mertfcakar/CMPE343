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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controls the logic for the user authentication (Login) screen.
 * <p>
 * This class handles user input validation, background authentication processing,
 * security measures (failed attempt lockout), and role-based redirection
 * to the appropriate dashboards.
 * </p>
 */
public class LoginController {

    // --- FXML UI Components ---
    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public TextField passwordTextField; // Visible text field for "Show Password"
    @FXML public CheckBox showPasswordCheck;
    @FXML public Label messageLabel;
    @FXML public Button loginButton;
    @FXML public ProgressIndicator loadingIndicator;
    @FXML public ImageView logoImage;
    @FXML public Label titleLabel;
    @FXML public Label subtitleLabel;
    @FXML public VBox mainContainer;

    /**
     * Counter for failed login attempts in the current session.
     */
    private int loginAttempts = 0;

    /**
     * Maximum allowed failed attempts before temporary lockout.
     */
    private final int MAX_ATTEMPTS = 3;

    /**
     * Countdown timer variable for the lockout duration.
     */
    private int remainingSeconds;

    /**
     * Initializes the controller class.
     * <p>
     * Sets up event listeners, specifically binding the "Enter" key
     * to the login action for better user experience.
     * </p>
     */
    @FXML
    public void initialize() {
        // Safe null-check before adding listeners
        if (passwordField != null) {
            passwordField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER)
                    handleLogin();
            });
        }

        if (passwordTextField != null) {
            passwordTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER)
                    handleLogin();
            });
        }
    }

    /**
     * Handles the login process when the user clicks the login button or presses Enter.
     * <p>
     * Workflow:
<<<<<<< HEAD
     * </p>
     * <ol>
     * <li>Validates that fields are not empty.</li>
     * <li>Disables UI and shows loading indicator.</li>
     * <li>Performs authentication in a background thread to prevent UI freezing.</li>
     * <li>On success: Redirects to the dashboard via {@link #openDashboard(User)}.</li>
     * <li>On failure: Increments retry counter and triggers {@link #startLockout()} if limit reached.</li>
     * </ol>
=======
     * <ol>
     *   <li>Validates that fields are not empty.</li>
     *   <li>Disables UI and shows loading indicator.</li>
     *   <li>Performs authentication in a background thread to prevent UI freezing.</li>
     *   <li>On success: Redirects to the dashboard via {@link #openDashboard(User)}.</li>
     *   <li>On failure: Increments retry counter and triggers {@link #startLockout()} if limit reached.</li>
     * </ol>
     * </p>
>>>>>>> b229e1c6e1976ed596a3e61a4421e674003c0746
     */
    @FXML
    public void handleLogin() {
        String u = usernameField.getText().trim();
        // Determine which field holds the current password input
        String p = showPasswordCheck.isSelected() ? passwordTextField.getText() : passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            showError("Username and Password cannot be empty!");
            return;
        }

        startLoading();

        // Run database operation in a background thread
        new Thread(() -> {
            try {
                User user = UserDAO.login(u, p);

                // Update UI back on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    stopLoading();

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
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    stopLoading();
                    showError("Login failed: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Redirects the authenticated user to the appropriate dashboard based on their role.
     *
     * @param user The authenticated {@link User} object.
     */
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
                // Default to Customer dashboard
                loader = new FXMLLoader(getClass().getResource("/fxml/customer.fxml"));
                root = loader.load();
                CustomerController controller = loader.getController();
                controller.initData(user);
                title = "GreenGrocer Market - " + user.getUsername();
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            
            // Maximize window for the main application
            stage.setMaximized(true); 
            
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("System Error: Could not load the dashboard.");
        }
    }

    // --- NAVIGATION METHODS ---

    /**
     * Navigates to the User Registration screen.
     *
     * @param event The action event triggering the navigation.
     */
    @FXML
    public void switchToRegister(ActionEvent event) {
        changeScene(event, "/fxml/register.fxml", "GreenGrocer - Register");
    }

    /**
     * Navigates to the Password Recovery screen.
     *
     * @param event The action event triggering the navigation.
     */
    @FXML
    public void switchToForgotPassword(ActionEvent event) {
        changeScene(event, "/fxml/forgot_password.fxml", "GreenGrocer - Reset Password");
    }

    /**
     * Utility method to switch the current scene.
     *
     * @param event    The event source (used to get the stage).
     * @param fxmlPath The resource path to the new FXML file.
     * @param title    The title of the new window.
     */
    private void changeScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Set default size for auth screens (1280x800)
            stage.setScene(new Scene(root, 1280, 800));
            
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading screen: " + fxmlPath);
        }
    }

    // --- HELPER METHODS ---

    /**
     * Initiates a security lockout preventing login attempts for 30 seconds.
     * <p>
     * This is triggered after {@code MAX_ATTEMPTS} is reached. A {@link Timeline}
     * is used to update the countdown label every second.
     * </p>
     */
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

    /**
     * Toggles password visibility between masked characters and plain text.
     * <p>
     * Syncs the text between {@code passwordField} (masked) and {@code passwordTextField} (visible).
     * </p>
     */
    @FXML
    public void handlePasswordVisibility() {
        if (showPasswordCheck.isSelected()) {
            // Switch to visible text
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            // Switch to masked text
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
    }

    /**
     * Displays an error message to the user in red.
     *
     * @param m The message string to display.
     */
    private void showError(String m) {
        messageLabel.setText(m);
        messageLabel.setTextFill(Color.RED);
    }

    /**
     * Sets the UI to a "Loading" state.
     * <p>
     * Disables input fields and buttons, and shows the progress indicator.
     * </p>
     */
    private void startLoading() {
        loginButton.setDisable(true);
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        passwordTextField.setDisable(true);
        showPasswordCheck.setDisable(true);
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);
        messageLabel.setText("Logging in...");
        messageLabel.setTextFill(Color.BLUE);
    }

    /**
     * Reverts the UI from the "Loading" state to the interactive state.
     */
    private void stopLoading() {
        loginButton.setDisable(false);
        usernameField.setDisable(false);
        passwordField.setDisable(false);
        passwordTextField.setDisable(false);
        showPasswordCheck.setDisable(false);
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        messageLabel.setText("");
    }
}