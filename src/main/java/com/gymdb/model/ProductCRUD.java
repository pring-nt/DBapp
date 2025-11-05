package com.gymdb.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class ProductCRUD {

    public boolean addRecord(Product product) {
        String sql = "INSERT INTO Product (productName, category, price, stockQty) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.productName());
            stmt.setString(2, product.category());
            stmt.setDouble(3, product.price());
            stmt.setInt(4, product.stockQty());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Product> getAllRecords() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM Product";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("productID"),
                        rs.getString("productName"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stockQty")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    public Product getRecord(int id) {
        String sql = "SELECT * FROM Product WHERE productID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Product(
                        rs.getInt("productID"),
                        rs.getString("productName"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stockQty")
                );
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean modRecord(Product product) {
        String sql = "UPDATE Product SET productName = ?, category = ?, price = ?, stockQty = ? WHERE productID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.productName());
            stmt.setString(2, product.category());
            stmt.setDouble(3, product.price());
            stmt.setInt(4, product.stockQty());
            stmt.setInt(5, product.productID());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean delRecord(int id) {
        String sql = "DELETE FROM Product WHERE productID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // TESTER
    // NOTE: only works when you have sample data.
    public static void main(String[] args) {
        ProductCRUD crud = new ProductCRUD();

        System.out.println("Adding record...");
        Product p1 = new Product(0, "Protein Bar", "Snacks", 99.99, 100);
        System.out.println(crud.addRecord(p1) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching productID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating productID = 1");
        Product updated = new Product(1, "Protein Shake", "Drinks", 149.50, 80);
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting productID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
