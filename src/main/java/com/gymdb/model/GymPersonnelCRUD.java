package com.gymdb.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class GymPersonnelCRUD {

    // CREATE
    public boolean addRecord(GymPersonnel personnel) {
        String sql = "INSERT INTO GymPersonnel (firstName, lastName, personnelType, schedule, instructorRecord, speciality) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, personnel.firstName());
            stmt.setString(2, personnel.lastName());
            stmt.setString(3, personnel.personnelType());
            stmt.setString(4, personnel.schedule());
            stmt.setString(5, personnel.instructorRecord());
            stmt.setString(6, personnel.speciality());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("addRecord error: " + e.getMessage());
            return false;
        }
    }

    // READ ALL
    public List<GymPersonnel> getAllRecords() {
        List<GymPersonnel> list = new ArrayList<>();
        String sql = "SELECT * FROM GymPersonnel";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                GymPersonnel gp = new GymPersonnel(
                        rs.getInt("personnelID"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("personnelType"),
                        rs.getString("schedule"),
                        rs.getString("instructorRecord"),
                        rs.getString("speciality")
                );
                list.add(gp);
            }

        } catch (SQLException e) {
            System.out.println("getAllRecords error: " + e.getMessage());
        }
        return list;
    }

    // READ ONE
    public GymPersonnel getRecord(int id) {
        String sql = "SELECT * FROM GymPersonnel WHERE personnelID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new GymPersonnel(
                        rs.getInt("personnelID"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("personnelType"),
                        rs.getString("schedule"),
                        rs.getString("instructorRecord"),
                        rs.getString("speciality")
                );
            }
        } catch (SQLException e) {
            System.out.println("getRecord error: " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean modRecord(GymPersonnel personnel) {
        String sql = "UPDATE GymPersonnel SET firstName=?, lastName=?, personnelType=?, schedule=?, instructorRecord=?, speciality=? WHERE personnelID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, personnel.firstName());
            stmt.setString(2, personnel.lastName());
            stmt.setString(3, personnel.personnelType());
            stmt.setString(4, personnel.schedule());
            stmt.setString(5, personnel.instructorRecord());
            stmt.setString(6, personnel.speciality());
            stmt.setInt(7, personnel.personnelID());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("modRecord error: " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean delRecord(int id) {
        String sql = "DELETE FROM GymPersonnel WHERE personnelID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("delRecord error: " + e.getMessage());
            return false;
        }
    }

    // TESTER
    // NOTE: only works when you have sample data.
    public static void main(String[] args) {
        GymPersonnelCRUD crud = new GymPersonnelCRUD();

        System.out.println("Adding record...");
        GymPersonnel newP = new GymPersonnel(0, "Juan", "Dela Cruz", "Trainer", "Mon-Fri 8-5", "Certified PT", "Strength Training");
        System.out.println(crud.addRecord(newP) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching personnelID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating personnelID = 1");
        GymPersonnel updated = new GymPersonnel(1, "Juan", "Cruz", "Head Trainer", "Mon-Fri 9-6", "Senior PT", "Muscle Growth");
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting personnelID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
