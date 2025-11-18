package com.gymdb.controller;

import com.gymdb.model.Locker;
import com.gymdb.model.LockerCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class LockerUsageReportController {

    private final LockerCRUD lockerCRUD = new LockerCRUD();

    @FXML
    private AnchorPane rootPane;

    @FXML
    private PieChart lockerUsageChart;

    @FXML
    private Button btnBack;

    @FXML
    public void initialize() {
        loadLockerUsageData();
    }

    private void loadLockerUsageData() {
        List<Locker> allLockers = lockerCRUD.getAllRecords();

        int active = 0;
        int overdue = 0;

        LocalDate today = LocalDate.now();

        for (Locker l : allLockers) {
            if (l.rentalEndDate() != null && !l.rentalEndDate().isBefore(today)) {
                active++;
            } else if (l.rentalEndDate() != null && l.rentalEndDate().isBefore(today) && "occupied".equalsIgnoreCase(l.status())) {
                overdue++;
            }
        }

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Active Rentals", active),
                new PieChart.Data("Overdue Rentals", overdue)
        );

        lockerUsageChart.setData(data);
        lockerUsageChart.setTitle("Locker Usage Report");
        lockerUsageChart.setLabelsVisible(true);
    }

    @FXML
    private void handleMemberActivity(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MembersReport.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePerformanceReward(ActionEvent event) throws IOException {
        // TODO: Load Performance Reward Report page
        System.out.println("Performance Reward clicked");
        // navigate(event, "/com/gymdb/reports/PerformanceReward.fxml");
    }

    @FXML
    private void handleRetailSales(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/RetailSalesReport.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
