package com.gymdb.controller;

import com.gymdb.model.ProductCRUD;
import com.gymdb.reports.RetailSalesReport;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;

public class RetailSalesReportController {

    @FXML
    private BarChart<String, Number> totalSalesBarChart;

    @FXML
    private PieChart avgSalesPieChart;

    private final ProductCRUD crud = new ProductCRUD();

    @FXML
    private void initialize() {
        loadCharts();
    }

    private void styleBarChartLabels() {
        // X-axis label
        totalSalesBarChart.getXAxis().lookup(".axis-label").setStyle("-fx-text-fill: white;");
        // Y-axis label
        totalSalesBarChart.getYAxis().lookup(".axis-label").setStyle("-fx-text-fill: white;");

        // Tick labels
        totalSalesBarChart.getXAxis().lookupAll(".tick-label").forEach(t -> t.setStyle("-fx-fill: white;"));
        totalSalesBarChart.getYAxis().lookupAll(".tick-label").forEach(t -> t.setStyle("-fx-fill: white;"));
    }

    private void stylePieChartLabels() {
        avgSalesPieChart.getData().forEach(data -> {
            if (data.getNode() != null) {
                data.getNode().lookupAll(".chart-pie-label").forEach(n -> n.setStyle("-fx-fill: white;"));
            }
        });
    }

    @FXML
    private void loadCharts() {
        var reports = crud.getRetailSalesSummary();

        // --- TOTAL SALES BAR CHART ---
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Sales (price × stock)");

        for (RetailSalesReport data : reports) {
            series.getData().add(new XYChart.Data<>(data.productCategory(), data.totalSales()));
        }

        totalSalesBarChart.getData().add(series);

        // Style bar chart labels after nodes are rendered
        Platform.runLater(() -> {
            // Axis labels
            totalSalesBarChart.getXAxis().lookup(".axis-label").setStyle("-fx-text-fill: white;");
            totalSalesBarChart.getYAxis().lookup(".axis-label").setStyle("-fx-text-fill: white;");

            // Tick labels
            totalSalesBarChart.getXAxis().lookupAll(".tick-label").forEach(t -> t.setStyle("-fx-fill: white;"));
            totalSalesBarChart.getYAxis().lookupAll(".tick-label").forEach(t -> t.setStyle("-fx-fill: white;"));

            // Bar values (if displayed)
            totalSalesBarChart.lookupAll(".chart-bar").forEach(bar -> {
                bar.lookupAll(".chart-bar-label").forEach(label -> label.setStyle("-fx-fill: white;"));
            });
        });

        // --- AVG SALES PIE CHART ---
        for (RetailSalesReport data : reports) {
            avgSalesPieChart.getData().add(new PieChart.Data(data.productCategory(), data.avgSalesPerProduct()));
        }

        // Style pie chart labels after nodes are rendered
        Platform.runLater(() -> {
            avgSalesPieChart.getData().forEach(data -> {
                if (data.getNode() != null) {
                    data.getNode().lookupAll(".chart-pie-label").forEach(label -> label.setStyle("-fx-fill: white;"));
                }
            });
        });
    }


    @FXML
    private void handleLockerUsage(ActionEvent event) throws IOException {
        System.out.println("Locker Usage clicked");
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
        System.out.println("Performance Reward clicked");
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
