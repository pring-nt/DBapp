package com.gymdb.model;
import java.time.LocalDateTime;

public record Attendance(
        int attendanceID,
        LocalDateTime datetime,
        int memberID,
        int classID
) {}

