package com.gymdb.model;

public class Product {
    private int productID;
    private String productName;
    private String category;
    private double price;
    private int stockQty;

    public Product(int productID, String productName, String category, double price, int stockQty) {
        this.productID = productID;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stockQty = stockQty;
    }

    // Getters
    public int productID() { return productID; }
    public String productName() { return productName; }
    public String category() { return category; }
    public double price() { return price; }
    public int stockQty() { return stockQty; }

    // Setters
    public void setProductID(int productID) { this.productID = productID; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
}
