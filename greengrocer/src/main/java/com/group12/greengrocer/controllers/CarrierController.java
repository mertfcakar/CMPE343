package com.group12.greengrocer.controllers;

import com.group12.greengrocer.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for carrier interface
 */
public class CarrierController {

    private User currentUser;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label statsLabel;

    @FXML
    private VBox availableDeliveriesBox;

    @FXML
    private VBox currentDeliveriesBox;

    @FXML
    private VBox completedDeliveriesBox;

    /**
     * Initialize controller with user data
     */
    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Carrier: " + user.getUsername());
        loadDeliveries();
    }

    @FXML
    private void initialize() {
        statusLabel.setText("Ready");
        statsLabel.setText("Current: 0 | Completed: 0 | Rating: N/A");
    }

    private void loadDeliveries() {
        // TODO: Load deliveries from database
        statusLabel.setText("Loading deliveries...");
    }

    @FXML
    private void handleRefreshAvailable() {
        statusLabel.setText("Refreshing available deliveries...");
        // TODO: Reload available deliveries
    }

    @FXML
    private void handleRefreshCurrent() {
        statusLabel.setText("Refreshing current deliveries...");
        // TODO: Reload current deliveries
    }

    @FXML
    private void handleSelectDelivery() {
        statusLabel.setText("Accepting delivery...");
        // TODO: Assign selected delivery to carrier
    }

    @FXML
    private void handleCompleteDelivery() {
        statusLabel.setText("Marking delivery as completed...");
        // TODO: Mark delivery as completed
    }

    @FXML
    private void handleViewRatings() {
        statusLabel.setText("Loading ratings...");
        // TODO: Display carrier's ratings
    }

    @FXML
    private void handleLogout() {
        try {
            // Close current window
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.close();

            // Open login window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Group12 GreenGrocer - Login");
            loginStage.setScene(new Scene(root, 960, 540));
            loginStage.centerOnScreen();
            loginStage.show();

            statusLabel.setText("Logged out successfully");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error logging out");
        }
    }
}