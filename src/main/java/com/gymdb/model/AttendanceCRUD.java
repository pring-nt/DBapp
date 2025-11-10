package com.gymdb.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class AttendanceCRUD {

    // create
    public boolean addRecord(Attendance attendance) {
        String sql = "INSERT INTO Attendance (memberID, classID, attendance_datetime) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attendance.memberID());
            stmt.setInt(2, attendance.classID());
            stmt.setTimestamp(3, Timestamp.valueOf(attendance.datetime())); // use record getter
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

    // get earliest attendance by classID
    public Attendance getEarliestByClassID(int classID) {
        String sql = "SELECT * FROM Attendance WHERE classID = ? ORDER BY attendance_datetime ASC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, classID);
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

    // count enrolled per class
    public int countByClassID(int classID) {
        String sql = "SELECT COUNT(*) FROM Attendance WHERE classID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, classID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("countByClassID error: " + e.getMessage());
        }
        return 0;
    }
}
