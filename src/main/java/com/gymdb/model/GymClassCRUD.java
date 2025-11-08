package com.gymdb.model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class GymClassCRUD {

    public boolean addRecord(GymClass gymClass) {
        String sql = "INSERT INTO Class (className, classType, scheduleDate, startTime, endTime, personnelID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, gymClass.className());
            stmt.setString(2, gymClass.classType());
            stmt.setDate(3, gymClass.scheduleDate() != null ? Date.valueOf(gymClass.scheduleDate()) : null);
            stmt.setTime(4, gymClass.startTime() != null ? Time.valueOf(gymClass.startTime()) : null);
            stmt.setTime(5, gymClass.endTime() != null ? Time.valueOf(gymClass.endTime()) : null);
            if (gymClass.personnelID() != null) stmt.setInt(6, gymClass.personnelID());
            else stmt.setNull(6, Types.INTEGER);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("addRecord error: " + e.getMessage());
            return false;
        }
    }

    public List<GymClass> getAllRecords() {
        List<GymClass> list = new ArrayList<>();
        String sql = "SELECT * FROM Class";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LocalDate date = rs.getDate("scheduleDate") != null ? rs.getDate("scheduleDate").toLocalDate() : null;
                LocalTime start = rs.getTime("startTime") != null ? rs.getTime("startTime").toLocalTime() : null;
                LocalTime end = rs.getTime("endTime") != null ? rs.getTime("endTime").toLocalTime() : null;

                list.add(new GymClass(
                        rs.getInt("classID"),
                        rs.getString("className"),
                        rs.getString("classType"),
                        date,
                        start,
                        end,
                        (Integer) rs.getObject("personnelID")
                ));
            }

        } catch (SQLException e) {
            System.err.println("getAllRecords error: " + e.getMessage());
        }

        return list;
    }

    public GymClass getRecord(int id) {
        String sql = "SELECT * FROM Class WHERE classID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate date = rs.getDate("scheduleDate") != null ? rs.getDate("scheduleDate").toLocalDate() : null;
                LocalTime start = rs.getTime("startTime") != null ? rs.getTime("startTime").toLocalTime() : null;
                LocalTime end = rs.getTime("endTime") != null ? rs.getTime("endTime").toLocalTime() : null;

                return new GymClass(
                        rs.getInt("classID"),
                        rs.getString("className"),
                        rs.getString("classType"),
                        date,
                        start,
                        end,
                        (Integer) rs.getObject("personnelID")
                );
            }

        } catch (SQLException e) {
            System.err.println("getRecord error: " + e.getMessage());
        }

        return null;
    }

    public boolean modRecord(GymClass gymClass) {
        String sql = "UPDATE Class SET className = ?, classType = ?, scheduleDate = ?, startTime = ?, endTime = ?, personnelID = ? WHERE classID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, gymClass.className());
            stmt.setString(2, gymClass.classType());
            stmt.setDate(3, gymClass.scheduleDate() != null ? Date.valueOf(gymClass.scheduleDate()) : null);
            stmt.setTime(4, gymClass.startTime() != null ? Time.valueOf(gymClass.startTime()) : null);
            stmt.setTime(5, gymClass.endTime() != null ? Time.valueOf(gymClass.endTime()) : null);
            if (gymClass.personnelID() != null) stmt.setInt(6, gymClass.personnelID());
            else stmt.setNull(6, Types.INTEGER);
            stmt.setInt(7, gymClass.classID());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("modRecord error: " + e.getMessage());
            return false;
        }
    }

    public boolean delRecord(int classID) {
        String sql = "DELETE FROM Class WHERE classID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, classID);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("delRecord error: " + e.getMessage());
            return false;
        }
    }

    // TESTER
    // NOTE: only works when you have sample data.
    // also note: delete only works when the class isn't referenced as a FK in another table hmm.
    public static void main(String[] args) {
        GymClassCRUD crud = new GymClassCRUD();

        System.out.println("Adding record...");
        GymClass g1 = new GymClass(0, "Morning Yoga", "Yoga", LocalDate.now(), LocalTime.of(7, 0), LocalTime.of(8, 0), 1);
        System.out.println(crud.addRecord(g1) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching classID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating classID = 1");
        GymClass updated = new GymClass(1, "Power Yoga", "Yoga", LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(9, 0), 2);
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting classID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
