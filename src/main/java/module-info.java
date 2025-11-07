module com.example.dbapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.gymdb.model to javafx.fxml;

    exports com.gymdb.test;
    opens com.gymdb.test to javafx.fxml, javafx.graphics;


    exports com.gymdb.utils;
    opens com.gymdb.utils to javafx.fxml;
    exports com.gymdb.controller;
    opens com.gymdb.controller to javafx.fxml;
    exports com.gymdb.launcher;
    opens com.gymdb.launcher to javafx.fxml, javafx.graphics;
}
