module com.example.dbapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.view to javafx.fxml;
    opens com.model to javafx.fxml;

    exports com.test;
    opens com.test to javafx.fxml, javafx.graphics;

    exports com.view;
    exports com.utils;
    opens com.utils to javafx.fxml;
    exports com.controller;
    opens com.controller to javafx.fxml;
}
