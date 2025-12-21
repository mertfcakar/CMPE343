module com.group12.greengrocer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;
    requires mysql.connector.j; 

    opens com.group12.greengrocer to javafx.graphics, javafx.fxml;
    opens com.group12.greengrocer.controllers to javafx.fxml;
    opens com.group12.greengrocer.models to javafx.base;

    exports com.group12.greengrocer;
    exports com.group12.greengrocer.controllers;
    exports com.group12.greengrocer.models;
}