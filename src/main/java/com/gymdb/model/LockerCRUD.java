package com.gymdb.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class LockerCRUD {

    public boolean addRecord(Locker locker) {
        String sql = "INSERT INTO Locker (status, rentalStartDate, rentalEndDate) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, locker.status());
            if (locker.rentalStartDate() != null)
                stmt.setDate(2, Date.valueOf(locker.rentalStartDate()));
            else
                stmt.setNull(2, Types.DATE);

            if (locker.rentalEndDate() != null)
                stmt.setDate(3, Date.valueOf(locker.rentalEndDate()));
            else
                stmt.setNull(3, Types.DATE);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Locker> getAllRecords() {
        List<Locker> list = new ArrayList<>();
        String sql = "SELECT * FROM Locker";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Locker l = new Locker(
                        rs.getInt("lockerID"),
                        rs.getString("status"),
                        rs.getDate("rentalStartDate") != null ? rs.getDate("rentalStartDate").toLocalDate() : null,
                        rs.getDate("rentalEndDate") != null ? rs.getDate("rentalEndDate").toLocalDate() : null
                );
                list.add(l);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public Locker getRecord(int id) {
        String sql = "SELECT * FROM Locker WHERE lockerID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Locker(
                        rs.getInt("lockerID"),
                        rs.getString("status"),
                        rs.getDate("rentalStartDate") != null ? rs.getDate("rentalStartDate").toLocalDate() : null,
                        rs.getDate("rentalEndDate") != null ? rs.getDate("rentalEndDate").toLocalDate() : null
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean modRecord(Locker locker) {
        String sql = "UPDATE Locker SET status = ?, rentalStartDate = ?, rentalEndDate = ? WHERE lockerID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, locker.status());

            if (locker.rentalStartDate() != null)
                stmt.setDate(2, Date.valueOf(locker.rentalStartDate()));
            else
                stmt.setNull(2, Types.DATE);

            if (locker.rentalEndDate() != null)
                stmt.setDate(3, Date.valueOf(locker.rentalEndDate()));
            else
                stmt.setNull(3, Types.DATE);

            stmt.setInt(4, locker.lockerID());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean delRecord(int id) {
        String sql = "DELETE FROM Locker WHERE lockerID = ?";
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
        LockerCRUD crud = new LockerCRUD();

        System.out.println("Adding record...");
        Locker l1 = new Locker(0, "occupied", LocalDate.now(), LocalDate.now().plusDays(30));
        System.out.println(crud.addRecord(l1) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching lockerID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating lockerID = 1");
        Locker updated = new Locker(1, "available", null, null);
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting lockerID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
