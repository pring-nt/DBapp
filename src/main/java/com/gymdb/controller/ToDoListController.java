package com.gymdb.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;

public class ToDoListController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> colDescription;
    @FXML private TableColumn<Task, String> colStatus;
    @FXML private TableColumn<Task, String> colUpdate;
    @FXML private TableColumn<Task, String> colRemove;

    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final File taskFile = new File("tasks.txt");

    private String currentUsername;

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    @FXML
    public void initialize() {
        loadTasksFromFile();
        taskTable.setItems(tasks);

        // Columns
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProperty());
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        // Update button
        colUpdate.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            { btn.setOnAction(e -> {
                int idx = getIndex();
                if (idx >= 0 && idx < tasks.size()) showTaskPopup(tasks.get(idx));
            }); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Remove button
        colRemove.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("X");
            { btn.setOnAction(e -> {
                int idx = getIndex();
                if (idx >= 0 && idx < tasks.size()) tasks.remove(idx);
            }); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    @FXML
    private void handleAddTask(ActionEvent e) {
        Task newTask = new Task("", "Not Started");
        showTaskPopup(newTask);
    }

    @FXML
    private void handleSave(ActionEvent e) {
        saveTasksToFile();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Tasks saved!");
        alert.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // --- Popup for Add/Edit ---
    private void showTaskPopup(Task task) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Task Editor");

        // Fields
        TextField descriptionField = new TextField(task.getDescription());
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("Not Started", "In Progress", "Done"));
        statusBox.setValue(task.getStatus());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Description:"), descriptionField);
        grid.addRow(1, new Label("Status:"), statusBox);
        dialog.getDialogPane().setContent(grid);

        // Buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> dialogButton);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                task.setDescription(descriptionField.getText());
                task.setStatus(statusBox.getValue());
                if (!tasks.contains(task)) tasks.add(task);
                taskTable.refresh();
            }
        });
    }

    // --- File Handling ---
    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(taskFile))) {
            for (Task t : tasks) {
                writer.write(t.getDescription().replace(";;", "") + ";;" + t.getStatus());
                writer.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadTasksFromFile() {
        if (!taskFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(taskFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";;");
                if (parts.length == 2) tasks.add(new Task(parts[0], parts[1]));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- Task Class ---
    public static class Task {
        private final javafx.beans.property.SimpleStringProperty description;
        private final javafx.beans.property.SimpleStringProperty status;

        public Task(String desc, String status) {
            this.description = new javafx.beans.property.SimpleStringProperty(desc);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public String getDescription() { return description.get(); }
        public void setDescription(String desc) { description.set(desc); }
        public javafx.beans.property.SimpleStringProperty descriptionProperty() { return description; }

        public String getStatus() { return status.get(); }
        public void setStatus(String s) { status.set(s); }
        public javafx.beans.property.SimpleStringProperty statusProperty() { return status; }
    }
}
