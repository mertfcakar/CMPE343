/**
 * Main module definition for the GreenGrocer application.
 * <p>
 * This module orchestrates the dependencies and package visibility required for the
 * application to function. It integrates JavaFX for the user interface, JDBC for
 * database connectivity, and external libraries for PDF generation.
 * </p>
 *
 * <h2>Key Dependencies:</h2>
 * <ul>
 * <li><b>javafx.controls, javafx.fxml:</b> Core JavaFX libraries for building the GUI.</li>
 * <li><b>java.sql, mysql.connector.j:</b> Required for database connections and JDBC operations.</li>
 * <li><b>itextpdf:</b> Used for generating PDF invoices for orders.</li>
 * <li><b>java.desktop:</b> Required for AWT/Swing integrations (often needed by image processing or PDF libraries).</li>
 * </ul>
 *
 * <h2>Package Access Configuration:</h2>
 * <p>
 * Certain packages are <code>opened</code> to allow JavaFX and other frameworks to access
 * classes via reflection (e.g., loading FXML controllers, reading model properties in TableViews).
 * </p>
 *
 * @author Group12
 */
module com.group12.greengrocer {
    // --- JavaFX Core Modules ---
    requires javafx.controls;
    requires javafx.fxml;

    // --- Database Modules ---
    requires java.sql;
    requires mysql.connector.j; 

    // --- Utility & System Modules ---
    requires java.desktop;
    requires itextpdf;

    // --- Open Packages for Reflection (JavaFX) ---
    // Opens the main package to graphics/fxml to allow the Application class to launch.
    opens com.group12.greengrocer to javafx.graphics, javafx.fxml;
    
    // Opens controllers to javafx.fxml to allow FXML loaders to inject @FXML annotated fields.
    opens com.group12.greengrocer.controllers to javafx.fxml;
    
    // Opens models to javafx.base to allow TableView/PropertyValueFactory to access getters via reflection.
    opens com.group12.greengrocer.models to javafx.base;

    // --- Exported Packages ---
    exports com.group12.greengrocer;
    exports com.group12.greengrocer.controllers;
    exports com.group12.greengrocer.models;
}