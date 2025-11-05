package com.gymdb.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class AttendanceCRUD {

    // create
    public boolean addRecord(Attendance attendance) {
        String sql = "INSERT INTO Attendance (memberID, classID) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attendance.memberID());
            stmt.setInt(2, attendance.classID());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // read all
    public List<Attendance> getAllRecords() {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("attendance_datetime");
                LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;

                Attendance a = new Attendance(
                        rs.getInt("attendanceID"),
                        dt,
                        rs.getInt("memberID"),
                        rs.getInt("classID")
                );
                list.add(a);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return list;
    }

    // read one
    public Attendance getRecord(int id) {
        String sql = "SELECT * FROM Attendance WHERE attendanceID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("attendance_datetime");
                LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;

                return new Attendance(
                        rs.getInt("attendanceID"),
                        dt,
                        rs.getInt("memberID"),
                        rs.getInt("classID")
                );
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    // update
    public boolean modRecord(int attendanceID, int newMemberID, int newClassID) {
        String sql = "UPDATE Attendance SET memberID = ?, classID = ? WHERE attendanceID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newMemberID);
            stmt.setInt(2, newClassID);
            stmt.setInt(3, attendanceID);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // delete
    public boolean delRecord(int attendanceID) {
        String sql = "DELETE FROM Attendance WHERE attendanceID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attendanceID);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // TESTER
    // NOTE: assumes that the gym_db contains stuff.
    public static void main(String[] args) {
        AttendanceCRUD crud = new AttendanceCRUD();

        // create
        System.out.println("Adding record...");
        Attendance newAttendance = new Attendance(0, null, 1, 2); // assume memberID=1, classID=2 exist
        boolean added = crud.addRecord(newAttendance);
        System.out.println(added ? "Record added successfully." : "Failed to add record.");

        // read all
        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        // read one
        System.out.println("\nFetching record with ID = 1");
        Attendance a = crud.getRecord(1);
        System.out.println(a);

        // update
        System.out.println("\nUpdating record with ID = 1");
        boolean updated = crud.modRecord(1, 3, 3); // new memberID=3, classID=3
        System.out.println(updated ? "Record updated successfully." : "Failed to update record.");


        // delete
        System.out.println("\nDeleting record with ID = 1");
        boolean deleted = crud.delRecord(5);
        System.out.println(deleted ? "Record deleted successfully." : "Failed to deleted record.");

    }

}
