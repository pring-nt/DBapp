module com.example.dbapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.repsandrecords.app to javafx.fxml;
    exports com.repsandrecords.app;
}
