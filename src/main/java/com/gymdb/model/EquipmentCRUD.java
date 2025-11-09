package com.gymdb.model;

import com.gymdb.utils.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EquipmentCRUD {

    // CREATE
    public boolean addRecord(Equipment e) {
        String sql = "INSERT INTO Equipment (equipment_name, equipment_description, quantity, unit_price, vendor, contact_no, purchase_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.equipmentName());
            ps.setString(2, e.equipmentDescription());
            ps.setInt(3, e.quantity());
            ps.setDouble(4, e.unitPrice());
            ps.setString(5, e.vendor());
            ps.setString(6, e.contactNo());
            ps.setTimestamp(7, Timestamp.valueOf(e.purchaseDate()));

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Error adding Equipment: " + ex.getMessage());
            return false;
        }
    }

    // READ ALL
    public List<Equipment> getAllRecords() {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT * FROM Equipment";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Equipment(
                        rs.getInt("equipmentID"),
                        rs.getString("equipment_name"),
                        rs.getString("equipment_description"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getString("vendor"),
                        rs.getString("contact_no"),
                        rs.getTimestamp("purchase_date").toLocalDateTime()
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error retrieving Equipment records: " + ex.getMessage());
        }
        return list;
    }

    // READ ONE
    public Equipment getRecord(int equipmentID) {
        String sql = "SELECT * FROM Equipment WHERE equipmentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, equipmentID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Equipment(
                            rs.getInt("equipmentID"),
                            rs.getString("equipment_name"),
                            rs.getString("equipment_description"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getString("vendor"),
                            rs.getString("contact_no"),
                            rs.getTimestamp("purchase_date").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching Equipment: " + ex.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean modRecord(Equipment e) {
        String sql = "UPDATE Equipment SET equipment_name = ?, equipment_description = ?, quantity = ?, unit_price = ?, vendor = ?, contact_no = ?, purchase_date = ? WHERE equipmentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.equipmentName());
            ps.setString(2, e.equipmentDescription());
            ps.setInt(3, e.quantity());
            ps.setDouble(4, e.unitPrice());
            ps.setString(5, e.vendor());
            ps.setString(6, e.contactNo());
            ps.setTimestamp(7, Timestamp.valueOf(e.purchaseDate()));
            ps.setInt(8, e.equipmentID());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Error updating Equipment: " + ex.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean delRecord(int equipmentID) {
        String sql = "DELETE FROM Equipment WHERE equipmentID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, equipmentID);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Error deleting Equipment: " + ex.getMessage());
            return false;
        }
    }

    // TESTER
    // NOTE: only works when you have sample data.
    public static void main(String[] args) {
        EquipmentCRUD crud = new EquipmentCRUD();

        System.out.println("Adding record...");
        Equipment eq = new Equipment(0, "Treadmill", "Electric treadmill with incline", 5, 1200.00, "FitnessPro Inc.", "09981234567", LocalDateTime.now());
        System.out.println(crud.addRecord(eq) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching equipmentID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating equipmentID = 1");
        Equipment updated = new Equipment(1, "Treadmill Plus", "Upgraded treadmill with touchscreen", 6, 1500.00, "FitnessPro Inc.", "09981234567", LocalDateTime.now());
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting equipmentID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
