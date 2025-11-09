package com.gymdb.launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CustomersProgressLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/CustomersProgress.fxml"));
        Parent root = loader.load();

        // Create the stage
        Stage stage = new Stage();
        stage.setTitle("Customer Progress");
        stage.setScene(new Scene(root));
        stage.setResizable(false); // optional: keep fixed size
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
