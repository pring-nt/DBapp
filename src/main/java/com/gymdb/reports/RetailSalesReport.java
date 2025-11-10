package com.gymdb.reports;

public record RetailSalesReport(
        String productCategory,
        int totalProducts,
        double totalSales,
        double avgSalesPerProduct
) {}
