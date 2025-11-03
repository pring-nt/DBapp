module com.example.dbapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.dbapp to javafx.fxml;
    exports com.example.dbapp;
}