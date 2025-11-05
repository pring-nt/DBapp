package com.gymdb.model;

public record Product(
        int productID,
        String productName,
        String category,
        double price,
        int stockQty
) {}
