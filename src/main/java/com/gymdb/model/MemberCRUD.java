package com.gymdb.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.gymdb.utils.DBConnection;

public class MemberCRUD {

    public boolean addRecord(Member m) {
        String sql =
                    "INSERT INTO Member (first_name, last_name, email, contact_no, membership_type," +
                    "start_date, end_date, health_goal, initial_weight, goal_weight," +
                    "start_bmi, updated_bmi, classID, trainerID, lockerID)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.firstName());
            stmt.setString(2, m.lastName());
            stmt.setString(3, m.email());
            stmt.setString(4, m.contactNo());
            stmt.setString(5, m.membershipType());
            stmt.setObject(6, m.startDate());
            stmt.setObject(7, m.endDate());
            stmt.setString(8, m.healthGoal());
            if (m.initialWeight() != null) stmt.setDouble(9, m.initialWeight()); else stmt.setNull(9, Types.DECIMAL);
            if (m.goalWeight() != null) stmt.setDouble(10, m.goalWeight()); else stmt.setNull(10, Types.DECIMAL);
            if (m.startBMI() != null) stmt.setDouble(11, m.startBMI()); else stmt.setNull(11, Types.DECIMAL);
            if (m.updatedBMI() != null) stmt.setDouble(12, m.updatedBMI()); else stmt.setNull(12, Types.DECIMAL);
            if (m.classID() != null) stmt.setInt(13, m.classID()); else stmt.setNull(13, Types.INTEGER);
            if (m.trainerID() != null) stmt.setInt(14, m.trainerID()); else stmt.setNull(14, Types.INTEGER);
            if (m.lockerID() != null) stmt.setInt(15, m.lockerID()); else stmt.setNull(15, Types.INTEGER);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Member> getAllRecords() {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT * FROM Member";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Member m = new Member(
                        rs.getInt("memberID"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("contact_no"),
                        rs.getString("membership_type"),
                        rs.getObject("start_date", LocalDate.class),
                        rs.getObject("end_date", LocalDate.class),
                        rs.getString("health_goal"),
                        rs.getObject("initial_weight") != null ? rs.getDouble("initial_weight") : null,
                        rs.getObject("goal_weight") != null ? rs.getDouble("goal_weight") : null,
                        rs.getObject("start_bmi") != null ? rs.getDouble("start_bmi") : null,
                        rs.getObject("updated_bmi") != null ? rs.getDouble("updated_bmi") : null,
                        rs.getObject("classID") != null ? rs.getInt("classID") : null,
                        rs.getObject("trainerID") != null ? rs.getInt("trainerID") : null,
                        rs.getObject("lockerID") != null ? rs.getInt("lockerID") : null
                );
                list.add(m);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public Member getRecord(int id) {
        String sql = "SELECT * FROM Member WHERE memberID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Member(
                        rs.getInt("memberID"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("contact_no"),
                        rs.getString("membership_type"),
                        rs.getObject("start_date", LocalDate.class),
                        rs.getObject("end_date", LocalDate.class),
                        rs.getString("health_goal"),
                        rs.getObject("initial_weight") != null ? rs.getDouble("initial_weight") : null,
                        rs.getObject("goal_weight") != null ? rs.getDouble("goal_weight") : null,
                        rs.getObject("start_bmi") != null ? rs.getDouble("start_bmi") : null,
                        rs.getObject("updated_bmi") != null ? rs.getDouble("updated_bmi") : null,
                        rs.getObject("classID") != null ? rs.getInt("classID") : null,
                        rs.getObject("trainerID") != null ? rs.getInt("trainerID") : null,
                        rs.getObject("lockerID") != null ? rs.getInt("lockerID") : null
                );
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean modRecord(Member m) {
        String sql =
                "UPDATE Member" +
                "SET first_name=?, last_name=?, email=?, contact_no=?, membership_type=?," +
                "start_date=?, end_date=?, health_goal=?, initial_weight=?, goal_weight=?," +
                "start_bmi=?, updated_bmi=?, classID=?, trainerID=?, lockerID=?" +
                "WHERE memberID=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.firstName());
            stmt.setString(2, m.lastName());
            stmt.setString(3, m.email());
            stmt.setString(4, m.contactNo());
            stmt.setString(5, m.membershipType());
            stmt.setObject(6, m.startDate());
            stmt.setObject(7, m.endDate());
            stmt.setString(8, m.healthGoal());
            if (m.initialWeight() != null) stmt.setDouble(9, m.initialWeight()); else stmt.setNull(9, Types.DECIMAL);
            if (m.goalWeight() != null) stmt.setDouble(10, m.goalWeight()); else stmt.setNull(10, Types.DECIMAL);
            if (m.startBMI() != null) stmt.setDouble(11, m.startBMI()); else stmt.setNull(11, Types.DECIMAL);
            if (m.updatedBMI() != null) stmt.setDouble(12, m.updatedBMI()); else stmt.setNull(12, Types.DECIMAL);
            if (m.classID() != null) stmt.setInt(13, m.classID()); else stmt.setNull(13, Types.INTEGER);
            if (m.trainerID() != null) stmt.setInt(14, m.trainerID()); else stmt.setNull(14, Types.INTEGER);
            if (m.lockerID() != null) stmt.setInt(15, m.lockerID()); else stmt.setNull(15, Types.INTEGER);
            stmt.setInt(16, m.memberID());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean delRecord(int id) {
        String sql = "DELETE FROM Member WHERE memberID = ?";
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
    // NOTE: only works when you have valid FK data (class, trainer, locker).
    public static void main(String[] args) {
        MemberCRUD crud = new MemberCRUD();

        System.out.println("Adding record...");
        Member m1 = new Member(0, "Alice", "Reyes", "alice@example.com", "09171234567",
                "Gold", LocalDate.now(), LocalDate.now().plusMonths(6), "Lose weight",
                70.5, 60.0, 24.5, 22.0, 1, 1, 1);
        System.out.println(crud.addRecord(m1) ? "Record added." : "Failed to add.");

        System.out.println("\nAll records:");
        crud.getAllRecords().forEach(System.out::println);

        System.out.println("\nFetching memberID = 1");
        System.out.println(crud.getRecord(1));

        System.out.println("\nUpdating memberID = 1");
        Member updated = new Member(1, "Alice", "Reyes", "alice_new@example.com", "09179876543",
                "Platinum", LocalDate.now(), LocalDate.now().plusMonths(12), "Gain muscle",
                65.0, 70.0, 22.0, 23.5, 2, 2, 2);
        System.out.println(crud.modRecord(updated) ? "Record updated." : "Update failed.");

        System.out.println("\nDeleting memberID = 4");
        System.out.println(crud.delRecord(4) ? "Record deleted." : "Delete failed.");
    }
}
