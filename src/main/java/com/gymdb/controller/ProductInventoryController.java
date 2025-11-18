package com.gymdb.controller;

import com.gymdb.model.Product;
import com.gymdb.model.ProductCRUD;
import com.gymdb.utils.DBConnection;
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
    private TableColumn<Product, Void> colEdit;

    @FXML
    private TableColumn<Product, Void> colAdd;
    @FXML
    private TableColumn<Product, Void> colDelete;


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

        // Add button column
        colAdd.setCellFactory(param -> new TableCell<>() {
            private final Button addBtn = new Button("Add");

            {
                addBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showAddDialog(); // Opens dialog to add new product
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(addBtn);
                }
            }
        });

// Delete button column
        colDelete.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDelete(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
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
    private void handleDelete(Product product) {
        if (product != null) {
            boolean deleted = crud.delRecord(product.productID());
            if (deleted) {
                productList.remove(product);
                showAlert("Deleted", "Product removed successfully.");
            } else {
                showAlert("Delete Failed", "Could not delete the selected product.");
            }
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

    private void showAddDialog() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add Product");
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        TextField priceField = new TextField();
        TextField stockField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Category:"), categoryField);
        grid.addRow(2, new Label("Price:"), priceField);
        grid.addRow(3, new Label("Stock Qty:"), stockField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Product newProduct = new Product(
                            0,
                            nameField.getText(),
                            categoryField.getText(),
                            Double.parseDouble(priceField.getText()),
                            Integer.parseInt(stockField.getText())
                    );
                    crud.addRecord(newProduct);
                    productList.add(newProduct);
                    return newProduct;
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