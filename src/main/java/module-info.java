module com.example.dbapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.view to javafx.fxml;
    exports com.view;
    exports com.utils;
    opens com.utils to javafx.fxml;
    exports com.controller;
    opens com.controller to javafx.fxml;
}
