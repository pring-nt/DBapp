package com.gymdb.reports;

import java.time.LocalDate;

public record PerformanceRewardReport(
        int memberID,
        String memberName,
        int totalSessions,
        String membershipType,
        LocalDate endDate,
        String qualificationStatus
) {}
