package com.gymdb.model;

import com.gymdb.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentCRUD {

    // CREATE
    public boolean addRecord(Payment payment) {
        String sql = "INSERT INTO Payment (payment_num, payment_date, transaction_type, amount, payment_method, memberID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payment.payment_num());
            ps.setTimestamp(2, payment.payment_date());
            ps.setString(3, payment.transaction_type());
            ps.setDouble(4, payment.amount());
            ps.setString(5, payment.payment_method());
            ps.setInt(6, payment.memberID());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // READ ALL
    public List<Payment> getAllRecords() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM Payment";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Payment(
                        rs.getInt("paymentID"),
                        rs.getString("payment_num"),
                        rs.getTimestamp("payment_date"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        rs.getInt("memberID")
                ));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    // READ ONE
    public Payment getRecord(int id) {
        String sql = "SELECT * FROM Payment WHERE paymentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Payment(
                            rs.getInt("paymentID"),
                            rs.getString("payment_num"),
                            rs.getTimestamp("payment_date"),
                            rs.getString("transaction_type"),
                            rs.getDouble("amount"),
                            rs.getString("payment_method"),
                            rs.getInt("memberID")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean modRecord(Payment payment) {
        String sql = "UPDATE Payment SET payment_num=?, payment_date=?, transaction_type=?, amount=?, payment_method=?, memberID=? WHERE paymentID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payment.payment_num());
            ps.setTimestamp(2, payment.payment_date());
            ps.setString(3, payment.transaction_type());
            ps.setDouble(4, payment.amount());
            ps.setString(5, payment.payment_method());
            ps.setInt(6, payment.memberID());
            ps.setInt(7, payment.paymentID());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean delRecord(int id) {
        String sql = "DELETE FROM Payment WHERE paymentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // TESTER
    public static void main(String[] args) {
        PaymentCRUD crud = new PaymentCRUD();

        System.out.println("Adding record...");
        Payment p1 = new Payment(0, "RCPT-1001", new Timestamp(System.currentTimeMillis()), "Membership", 1500.00, "Credit Card", 1);
        System.out.println(crud.addRecord(p1) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching paymentID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating paymentID = 1");
        Payment updated = new Payment(1, "RCPT-1001-EDIT", new Timestamp(System.currentTimeMillis()), "Product", 2000.00, "Cash", 1);
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting paymentID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
