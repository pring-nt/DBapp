package com.gymdb.services;

import com.gymdb.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ClassAttendanceService {

    /**
     * Retrieves a list of all class attendance records, joined with their class types.
     * Each result combines data from the Attendance and Class tables.
     * returns a list of class attendances containing attendance records and the corresponding class types
     * returns an empty list if none are found or if an error occurs
     */
    public static List<ClassAttendance> getClassAttendances(){
        List<ClassAttendance> list = new ArrayList<>();
        String sql = """
                SELECT 	a.attendanceID,
                		a.attendance_datetime,
                        a.memberID,
                        c.classType
                FROM 	Attendance a JOIN Class c ON a.classID = c.classID
                """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("attendance_datetime");
                LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;

                ClassAttendance a = new ClassAttendance(
                        rs.getInt("attendanceID"),
                        dt,
                        rs.getInt("memberID"),
                        rs.getString("classType")
                );
                list.add(a);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return list;
    }
}
