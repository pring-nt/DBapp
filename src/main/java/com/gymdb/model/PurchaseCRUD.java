package com.gymdb.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class PurchaseCRUD {

    public boolean addRecord(Purchase purchase) {
        String sql = "INSERT INTO Purchase (quantity, memberID, productID) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchase.quantity());
            stmt.setInt(2, purchase.memberID());
            stmt.setInt(3, purchase.productID());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public List<Purchase> getAllRecords() {
        List<Purchase> list = new ArrayList<>();
        String sql = "SELECT * FROM Purchase";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("purchase_date");
                LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;

                Purchase p = new Purchase(
                        rs.getInt("purchaseID"),
                        dt,
                        rs.getInt("quantity"),
                        rs.getInt("memberID"),
                        rs.getInt("productID")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return list;
    }

    public Purchase getRecord(int id) {
        String sql = "SELECT * FROM Purchase WHERE purchaseID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("purchase_date");
                LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;

                return new Purchase(
                        rs.getInt("purchaseID"),
                        dt,
                        rs.getInt("quantity"),
                        rs.getInt("memberID"),
                        rs.getInt("productID")
                );
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    public boolean modRecord(Purchase purchase) {
        String sql = "UPDATE Purchase SET quantity = ?, memberID = ?, productID = ? WHERE purchaseID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchase.quantity());
            stmt.setInt(2, purchase.memberID());
            stmt.setInt(3, purchase.productID());
            stmt.setInt(4, purchase.purchaseID());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean delRecord(int purchaseID) {
        String sql = "DELETE FROM Purchase WHERE purchaseID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseID);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // TESTER
    // NOTE: only works when you have sample data.
    // also note: delete only works when the purchase isn't referenced as a FK elsewhere.
    public static void main(String[] args) {
        PurchaseCRUD crud = new PurchaseCRUD();

        System.out.println("Adding record...");
        Purchase p1 = new Purchase(0, null, 3, 1, 2); // quantity=3, memberID=1, productID=2
        System.out.println(crud.addRecord(p1) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching purchaseID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating purchaseID = 1");
        Purchase updated = new Purchase(1, null, 5, 1, 3);
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting purchaseID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
