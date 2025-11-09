package com.gymdb.controller;

import com.gymdb.model.AttendanceCRUD;
import com.gymdb.model.GymClass;
import com.gymdb.model.GymClassCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassController {

    @FXML private TableView<GymClassTableRow> tblClasses;
    @FXML private TableColumn<GymClassTableRow, Integer> colClassID;
    @FXML private TableColumn<GymClassTableRow, String> colClassName;
    @FXML private TableColumn<GymClassTableRow, String> colScheduleDate;
    @FXML private TableColumn<GymClassTableRow, String> colStartTime;
    @FXML private TableColumn<GymClassTableRow, String> colEndTime;
    @FXML private TableColumn<GymClassTableRow, Integer> colPersonnelID;
    @FXML private TableColumn<GymClassTableRow, Integer> colEnrolled;
    @FXML private Button backBtn;

    private final GymClassCRUD gymClassCRUD = new GymClassCRUD();
    private final AttendanceCRUD attendanceCRUD = new AttendanceCRUD();
    private final ObservableList<GymClassTableRow> classRows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // map table columns
        colClassID.setCellValueFactory(data -> data.getValue().classIDProperty().asObject());
        colClassName.setCellValueFactory(data -> data.getValue().classNameProperty());
        colScheduleDate.setCellValueFactory(data -> data.getValue().scheduleDateProperty());
        colStartTime.setCellValueFactory(data -> data.getValue().startTimeProperty());
        colEndTime.setCellValueFactory(data -> data.getValue().endTimeProperty());
        colPersonnelID.setCellValueFactory(data -> data.getValue().personnelIDProperty().asObject());
        colEnrolled.setCellValueFactory(data -> data.getValue().enrolledProperty().asObject());

        loadTableData();
    }

    private void loadTableData() {
        classRows.clear();
        List<GymClass> classes = gymClassCRUD.getAllRecords();

        // get enrolled counts per class
        Map<Integer, Integer> enrolledCounts = new HashMap<>();
        for (GymClass g : classes) {
            enrolledCounts.put(g.classID(), attendanceCRUD.countByClassID(g.classID()));
        }

        for (GymClass g : classes) {
            classRows.add(new GymClassTableRow(
                    g.classID(),
                    g.className(),
                    g.scheduleDate() != null ? g.scheduleDate().toString() : "",
                    g.startTime() != null ? g.startTime().toString() : "",
                    g.endTime() != null ? g.endTime().toString() : "",
                    g.personnelID() != null ? g.personnelID() : 0,
                    enrolledCounts.getOrDefault(g.classID(), 0)
            ));
        }

        tblClasses.setItems(classRows);
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml")); // adjust path
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Inner class for table row representation
    public static class GymClassTableRow {
        private final javafx.beans.property.IntegerProperty classID;
        private final javafx.beans.property.StringProperty className;
        private final javafx.beans.property.StringProperty scheduleDate;
        private final javafx.beans.property.StringProperty startTime;
        private final javafx.beans.property.StringProperty endTime;
        private final javafx.beans.property.IntegerProperty personnelID;
        private final javafx.beans.property.IntegerProperty enrolled;

        public GymClassTableRow(int classID, String className, String scheduleDate,
                                String startTime, String endTime, int personnelID, int enrolled) {
            this.classID = new javafx.beans.property.SimpleIntegerProperty(classID);
            this.className = new javafx.beans.property.SimpleStringProperty(className);
            this.scheduleDate = new javafx.beans.property.SimpleStringProperty(scheduleDate);
            this.startTime = new javafx.beans.property.SimpleStringProperty(startTime);
            this.endTime = new javafx.beans.property.SimpleStringProperty(endTime);
            this.personnelID = new javafx.beans.property.SimpleIntegerProperty(personnelID);
            this.enrolled = new javafx.beans.property.SimpleIntegerProperty(enrolled);
        }

        public javafx.beans.property.IntegerProperty classIDProperty() { return classID; }
        public javafx.beans.property.StringProperty classNameProperty() { return className; }
        public javafx.beans.property.StringProperty scheduleDateProperty() { return scheduleDate; }
        public javafx.beans.property.StringProperty startTimeProperty() { return startTime; }
        public javafx.beans.property.StringProperty endTimeProperty() { return endTime; }
        public javafx.beans.property.IntegerProperty personnelIDProperty() { return personnelID; }
        public javafx.beans.property.IntegerProperty enrolledProperty() { return enrolled; }
    }

}
