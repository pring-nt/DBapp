package com.gymdb.controller;

import com.gymdb.reports.RetailSalesReport;
import com.gymdb.services.ReportService;

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

    @FXML
    private void initialize() {
        // Apply your chart CSS
        totalSalesBarChart.getStylesheets().add(
                getClass().getResource("/css/charts.css").toExternalForm()
        );
        avgSalesPieChart.getStylesheets().add(
                getClass().getResource("/css/charts.css").toExternalForm()
        );

        // Load chart data
        loadCharts();

        // Ensure pie chart labels exist (color handled later if needed)
        totalSalesBarChart.getXAxis().setTickLabelFill(javafx.scene.paint.Color.WHITE);
        totalSalesBarChart.getYAxis().setTickLabelFill(javafx.scene.paint.Color.WHITE);

        avgSalesPieChart.setLabelsVisible(true);

        Platform.runLater(() -> {
            totalSalesBarChart.getXAxis().lookupAll(".tick-label")
                    .forEach(n -> n.setStyle("-fx-fill: black;"));

            totalSalesBarChart.lookupAll(".chart-legend-item")
                    .forEach(n -> n.lookupAll(".label")
                            .forEach(l -> l.setStyle("-fx-text-fill: black;")));
        });
    }


    @FXML
    private void loadCharts() {
        // use ReportService instead of ProductCRUD
        var reports = ReportService.getRetailSalesReports();
        totalSalesBarChart.getData().clear();
        avgSalesPieChart.getData().clear();


        /* --------------------------
           TOTAL SALES BAR CHART
        ---------------------------- */
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Sales");

        for (RetailSalesReport data : reports) {
            series.getData().add(new XYChart.Data<>(
                    data.productCategory(),
                    data.totalSales()
            ));
        }

        totalSalesBarChart.getData().add(series);

        /* --------------------------
           AVG SALES PIE CHART
        ---------------------------- */
        for (RetailSalesReport data : reports) {
            avgSalesPieChart.getData().add(
                    new PieChart.Data(
                            data.productCategory(),
                            data.avgSalesPerProduct()
                    )
            );
        }
    }

    /* --------------------------
       BUTTON HANDLERS
    ---------------------------- */

    @FXML
    private void handleLockerUsage(ActionEvent event) {
        System.out.println("Locker Usage clicked");
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
    private void handlePerformanceReward(ActionEvent event) {
        System.out.println("Performance Reward clicked");
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
