package com.gymdb.controller;

import com.gymdb.model.Product;
import com.gymdb.model.ProductCRUD;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class ProductInventoryController {

    @FXML
    private Button backbtn;

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
    private TableColumn<Product, Void> colEdit; // New column for Edit button

    private ProductCRUD crud = new ProductCRUD();
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Set up the table columns
        colID.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().productID()));
        colName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().productName()));
        colCategory.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().category()));
        colPrice.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().price()));
        colStock.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().stockQty()));

        // Add Edit button column
        colEdit.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");

            {
                editBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showEditDialog(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editBtn);
                }
            }
        });

        // Load products from DB
        ObservableList<Product> dbProducts = FXCollections.observableArrayList(crud.getAllRecords());

        if (dbProducts.isEmpty()) {
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

    // Back button handler
    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/MainMenu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Delete selected product
    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean deleted = crud.delRecord(selected.productID());
            if (deleted) {
                productList.remove(selected);
                showAlert("Deleted", "Product removed successfully.");
            } else {
                showAlert("Delete Failed", "Could not delete the selected product.");
            }
        } else {
            showAlert("No Selection", "Please select a product to delete.");
        }
    }

    // Buy product and reduce stock
    @FXML
    private void handleBuy() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Purchase Product");
            dialog.setHeaderText("Enter quantity to buy:");
            dialog.setContentText("Quantity:");

            dialog.showAndWait().ifPresent(input -> {
                try {
                    int qty = Integer.parseInt(input);
                    if (qty <= 0) {
                        showAlert("Invalid Quantity", "Please enter a number greater than 0.");
                    } else if (qty > selected.stockQty()) {
                        showAlert("Insufficient Stock", "Not enough stock available.");
                    } else {
                        selected.setStockQty(selected.stockQty() - qty);
                        crud.modRecord(selected);
                        productTable.refresh();
                        showAlert("Purchase Successful", qty + " units purchased.");
                    }
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid number.");
                }
            });
        } else {
            showAlert("No Selection", "Please select a product to buy.");
        }
    }

    // Edit product dialog
    private void showEditDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(product.productName());
        TextField categoryField = new TextField(product.category());
        TextField priceField = new TextField(String.valueOf(product.price()));
        TextField stockField = new TextField(String.valueOf(product.stockQty()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Category:"), categoryField);
        grid.addRow(2, new Label("Price:"), priceField);
        grid.addRow(3, new Label("Stock Qty:"), stockField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    product.setProductName(nameField.getText());
                    product.setCategory(categoryField.getText());
                    product.setPrice(Double.parseDouble(priceField.getText()));
                    product.setStockQty(Integer.parseInt(stockField.getText()));

                    crud.modRecord(product);
                    productTable.refresh();
                    return product;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numeric values for price and stock.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
