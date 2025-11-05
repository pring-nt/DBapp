package com.gymdb.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class MemberFeedbackCRUD {

    // CREATE
    public boolean addRecord(MemberFeedback feedback) {
        String sql = "INSERT INTO MemberFeedback (comments, personnelID, memberID) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, feedback.comments());
            stmt.setInt(2, feedback.personnelID());
            stmt.setInt(3, feedback.memberID());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // READ ALL
    public List<MemberFeedback> getAllRecords() {
        List<MemberFeedback> list = new ArrayList<>();
        String sql = "SELECT * FROM MemberFeedback";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MemberFeedback f = new MemberFeedback(
                        rs.getInt("feedbackID"),
                        rs.getString("comments"),
                        rs.getInt("personnelID"),
                        rs.getInt("memberID")
                );
                list.add(f);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    // READ ONE
    public MemberFeedback getRecord(int id) {
        String sql = "SELECT * FROM MemberFeedback WHERE feedbackID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new MemberFeedback(
                        rs.getInt("feedbackID"),
                        rs.getString("comments"),
                        rs.getInt("personnelID"),
                        rs.getInt("memberID")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean modRecord(MemberFeedback feedback) {
        String sql = "UPDATE MemberFeedback SET comments = ?, personnelID = ?, memberID = ? WHERE feedbackID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, feedback.comments());
            stmt.setInt(2, feedback.personnelID());
            stmt.setInt(3, feedback.memberID());
            stmt.setInt(4, feedback.feedbackID());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean delRecord(int feedbackID) {
        String sql = "DELETE FROM MemberFeedback WHERE feedbackID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, feedbackID);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // TESTER
    // NOTE: only works when you have sample data and valid member/personnel IDs.
    public static void main(String[] args) {
        MemberFeedbackCRUD crud = new MemberFeedbackCRUD();

        System.out.println("Adding record...");
        MemberFeedback f1 = new MemberFeedback(0, "Trainer was very supportive!", 1, 1);
        System.out.println(crud.addRecord(f1) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching feedbackID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating feedbackID = 1");
        MemberFeedback updated = new MemberFeedback(1, "Trainer was excellent and encouraging!", 1, 1);
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting feedbackID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
