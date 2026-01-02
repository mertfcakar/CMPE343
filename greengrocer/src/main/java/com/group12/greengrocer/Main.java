package com.group12.greengrocer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The entry point class for the Group12 GreenGrocer application.
 * <p>
 * This class extends {@link Application} to initialize the JavaFX runtime environment.
 * It is responsible for loading the initial user interface (Login Screen) and
 * setting up the primary stage window properties.
 * </p>
 * <p>
 * <b>Course:</b> CMPE 343 - Fall 2025-2026<br>
 * <b>Project:</b> Project 3
 * </p>
 */
public class Main extends Application {

    /**
     * Starts the JavaFX application.
     * <p>
     * This method is called by the JavaFX runtime after the system is ready.
     * It loads the {@code login.fxml} resource, configures the application window
     * dimensions (1200x900), sets the title, and displays the primary stage.
     * </p>
     *
     * @param primaryStage The primary window (stage) provided by the JavaFX platform.
     * @throws Exception If the FXML file cannot be found or loaded properly.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the login FXML
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));

        Scene scene = new Scene(root, 1200, 900);

        primaryStage.setTitle("Group12 GreenGrocer - Login");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * The standard Java entry point for the application.
     * <p>
     * This method calls {@link #launch(String...)} to begin the JavaFX application lifecycle.
     * </p>
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}