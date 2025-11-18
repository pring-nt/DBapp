package com.gymdb.controller;

import com.gymdb.reports.LockerUsageReport;
import com.gymdb.services.ReportService;
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
import java.util.List;

public class LockerUsageReportController {

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
        List<LockerUsageReport> reports = ReportService.getLockerUsageReports();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        for (LockerUsageReport r : reports) {
            System.out.println("Category: " + r.category() + ", Count: " + r.lockerCount()); // debug

            if (r.lockerCount() > 0) {
                data.add(new PieChart.Data(r.category() + " (" + r.lockerCount() + ")", r.lockerCount()));
            }
        }

        lockerUsageChart.setData(data);
        lockerUsageChart.setTitle("Locker Usage Report");
        lockerUsageChart.setLabelsVisible(true);
    }

    @FXML
    private void handleMemberActivity(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MembersReport.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handlePerformanceReward(ActionEvent event) throws IOException {
        System.out.println("Performance Reward clicked");
        // TODO: Navigate to Performance Reward report
    }

    @FXML
    private void handleRetailSales(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/RetailSalesReport.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MainMenu.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
