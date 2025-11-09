package com.gymdb.services;

import java.time.LocalDateTime;

public record ClassAttendance (
        int attendanceID,
        LocalDateTime attendanceDateTime,
        int memberID,
        String classType
) {}
