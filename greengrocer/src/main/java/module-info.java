module com.group12.greengrocer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;

    // Open packages to javafx.fxml for reflection
    opens com.group12.greengrocer to javafx.fxml;
    opens com.group12.greengrocer.controllers to javafx.fxml;
    
    // Export packages
    exports com.group12.greengrocer;
    exports com.group12.greengrocer.controllers;
}