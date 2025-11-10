package com.gymdb.services;

import com.gymdb.reports.LockerUsageReport;
import com.gymdb.reports.PerformanceRewardReport;
import com.gymdb.reports.RetailSalesReport;
import com.gymdb.utils.DBConnection;

import com.gymdb.reports.MemberActivityReport;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    /**
     * Retrieves member progress report rows using the DB schema fields:
     * sessions attended, initial/goal weight, BMI change/trend, and the member's stated health goal.
     *
     * @return List<MemberActivityReport> empty list on error or when no data exists
     */
    public static List<MemberActivityReport> getMemberProgressReports() {
        List<MemberActivityReport> list = new ArrayList<>();

        String sql = """
            SELECT
                m.memberID,
                CONCAT(m.first_name, ' ', m.last_name) AS member_name,
                COUNT(a.attendanceID) AS sessions_attended,
                m.initial_weight,
                m.goal_weight,
                (m.initial_weight - m.goal_weight) AS target_weight_change,
                m.start_bmi,
                m.updated_bmi,
                (m.updated_bmi - m.start_bmi) AS bmi_change,
                CASE
                    WHEN m.updated_bmi < m.start_bmi THEN 'Improving'
                    WHEN m.updated_bmi > m.start_bmi THEN 'Worsening'
                    ELSE 'Stable'
                END AS bmi_trend,
                m.health_goal
            FROM Member m
            LEFT JOIN Attendance a ON m.memberID = a.memberID
            GROUP BY
                m.memberID, m.first_name, m.last_name, m.initial_weight, m.goal_weight,
                m.start_bmi, m.updated_bmi, m.health_goal
            ORDER BY sessions_attended DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int memberID = rs.getInt("memberID");
                String memberName = rs.getString("member_name");
                int sessionsAttended = rs.getInt("sessions_attended");

                // nullable numeric columns — default to 0.0 if NULL
                Double initialWeightObj = rs.getObject("initial_weight", Double.class);
                double initialWeight = initialWeightObj != null ? initialWeightObj : 0.0;

                Double goalWeightObj = rs.getObject("goal_weight", Double.class);
                double goalWeight = goalWeightObj != null ? goalWeightObj : 0.0;

                Double targetWeightChangeObj = rs.getObject("target_weight_change", Double.class);
                double targetWeightChange = targetWeightChangeObj != null ? targetWeightChangeObj : 0.0;

                Double startBMIObj = rs.getObject("start_bmi", Double.class);
                double startBMI = startBMIObj != null ? startBMIObj : 0.0;

                Double updatedBMIObj = rs.getObject("updated_bmi", Double.class);
                double updatedBMI = updatedBMIObj != null ? updatedBMIObj : 0.0;

                Double bmiChangeObj = rs.getObject("bmi_change", Double.class);
                double bmiChange = bmiChangeObj != null ? bmiChangeObj : 0.0;

                String bmiTrend = rs.getString("bmi_trend");
                String healthGoal = rs.getString("health_goal");

                MemberActivityReport rpt = new MemberActivityReport(
                        memberID,
                        memberName,
                        sessionsAttended,
                        initialWeight,
                        goalWeight,
                        targetWeightChange,
                        startBMI,
                        updatedBMI,
                        bmiChange,
                        bmiTrend,
                        healthGoal
                );

                list.add(rpt);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching member progress reports: " + e.getMessage());
        }

        return list;
    }

    public static List<PerformanceRewardReport> getPerformanceRewardReports() {
        List<PerformanceRewardReport> list = new ArrayList<>();

        String sql = """
        SELECT
            m.memberID,
            CONCAT(m.first_name, ' ', m.last_name) AS member_name,
            COUNT(a.attendanceID) AS total_sessions,
            m.membership_type,
            m.end_date,
            CASE
                WHEN COUNT(a.attendanceID) >= 10 THEN 'Qualified'
                ELSE 'Not Qualified'
            END AS qualification_status
        FROM Member m
        LEFT JOIN Attendance a ON m.memberID = a.memberID
        GROUP BY
            m.memberID, m.first_name, m.last_name, m.membership_type, m.end_date
        HAVING COUNT(a.attendanceID) >= 1
        ORDER BY total_sessions DESC
       """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int memberID = rs.getInt("memberID");
                String memberName = rs.getString("member_name");
                int totalSessions = rs.getInt("total_sessions");
                String membershipType = rs.getString("membership_type");

                // nullable date column
                Date endDateSql = rs.getDate("end_date");
                LocalDate endDate = endDateSql != null ? endDateSql.toLocalDate() : null;

                String qualificationStatus = rs.getString("qualification_status");

                PerformanceRewardReport rpt = new PerformanceRewardReport(
                        memberID,
                        memberName,
                        totalSessions,
                        membershipType,
                        endDate,
                        qualificationStatus
                );

                list.add(rpt);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching performance reward reports: " + e.getMessage());
        }

        return list;
    }

    public static List<LockerUsageReport> getLockerUsageReports() {
        List<LockerUsageReport> list = new ArrayList<>();

        String sql = """
        SELECT
            CASE
                WHEN status = 'rented' AND rentalEndDate >= CURDATE() THEN 'Active Rental'
                WHEN status = 'rented' AND rentalEndDate < CURDATE() THEN 'Overdue Rental'
                ELSE 'Available'
            END AS category,
            COUNT(*) AS locker_count,
            CONCAT(ROUND(COUNT(*) / (SELECT COUNT(*) FROM Locker) * 100, 1), '%') AS percent_of_total
        FROM Locker
        GROUP BY category
        ORDER BY FIELD(category, 'Active Rental', 'Overdue Rental', 'Available')
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String category = rs.getString("category");
                int lockerCount = rs.getInt("locker_count");
                String percentOfTotal = rs.getString("percent_of_total");

                LockerUsageReport rpt = new LockerUsageReport(
                        category,
                        lockerCount,
                        percentOfTotal
                );

                list.add(rpt);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching locker usage reports: " + e.getMessage());
        }

        return list;
    }

    public static List<RetailSalesReport> getRetailSalesReports() {
        List<RetailSalesReport> list = new ArrayList<>();

        String sql = """
        SELECT
            p.category AS product_category,
            COUNT(DISTINCT p.productID) AS total_products,
            SUM(pr.quantity * p.price) AS total_sales,
            ROUND(AVG(pr.quantity * p.price), 2) AS avg_sales_per_product
        FROM Purchase pr
        JOIN Product p ON pr.productID = p.productID
        GROUP BY p.category
        ORDER BY total_sales DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String productCategory = rs.getString("product_category");
                int totalProducts = rs.getInt("total_products");

                // nullable numeric columns — default to 0.0 if NULL
                Double totalSalesObj = rs.getObject("total_sales", Double.class);
                double totalSales = totalSalesObj != null ? totalSalesObj : 0.0;

                Double avgSalesObj = rs.getObject("avg_sales_per_product", Double.class);
                double avgSalesPerProduct = avgSalesObj != null ? avgSalesObj : 0.0;

                RetailSalesReport rpt = new RetailSalesReport(
                        productCategory,
                        totalProducts,
                        totalSales,
                        avgSalesPerProduct
                );

                list.add(rpt);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching retail sales reports: " + e.getMessage());
        }

        return list;
    }
}
