package com.gymdb.controller;

import com.gymdb.model.Product;
import com.gymdb.model.ProductCRUD;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class BuyProductController {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> colID;

    @FXML
    private TableColumn<Product, String> colName;

    @FXML
    private TableColumn<Product, String> colCategory;

    @FXML
    private TableColumn<Product, Double> colPrice;

    @FXML
    private TableColumn<Product, Integer> colStock;

    @FXML
    private TableColumn<Product, Void> colBuy; // Last column = Buy

    private ProductCRUD crud = new ProductCRUD();
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    private String currentUsername;

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    @FXML
    private void initialize() {
        // Set up columns
        colID.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().productID()));
        colName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().productName()));
        colCategory.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().category()));
        colPrice.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().price()));
        colStock.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().stockQty()));

        // Buy button column
        colBuy.setCellFactory(param -> new TableCell<>() {
            private final Button buyBtn = new Button("Buy");

            {
                buyBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showBuyDialog(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buyBtn);
                }
            }
        });

        // Load products from DB
        ObservableList<Product> dbProducts = FXCollections.observableArrayList(crud.getAllRecords());
        if (dbProducts.isEmpty()) {
            // Add sample products if DB empty
            Product p1 = new Product(0, "Protein Bar", "Snacks", 99.99, 100);
            Product p2 = new Product(0, "Protein Shake", "Drinks", 149.50, 80);
            Product p3 = new Product(0, "Chocolate Shake", "Beverage", 120.00, 50);
            Product p4 = new Product(0, "Energy Drink", "Beverage", 150.00, 30);
            crud.addRecord(p1);
            crud.addRecord(p2);
            crud.addRecord(p3);
            crud.addRecord(p4);
            productList.addAll(p1, p2, p3, p4);
        } else {
            productList.addAll(dbProducts);
        }

        productTable.setItems(productList);
    }

    private void showBuyDialog(Product product) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Buy Product");
        dialog.setHeaderText("Enter quantity to buy for: " + product.productName());
        dialog.setContentText("Quantity:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input);
                if (qty <= 0) {
                    showAlert("Invalid Quantity", "Enter a number greater than 0.");
                } else if (qty > product.stockQty()) {
                    showAlert("Insufficient Stock", "Not enough stock available.");
                } else {
                    product.setStockQty(product.stockQty() - qty);
                    crud.modRecord(product); // Update DB
                    productTable.refresh();   // Refresh table
                    showAlert("Purchase Successful", qty + " units of " + product.productName() + " bought.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/CustomersDashboard.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
