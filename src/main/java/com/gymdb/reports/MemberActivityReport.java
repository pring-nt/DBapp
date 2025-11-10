package com.gymdb.reports;

public record MemberActivityReport(
        int memberID,
        String memberName,
        int sessionsAttended,
        double initialWeight,
        double goalWeight,
        double targetWeightChange,
        double startBMI,
        double updatedBMI,
        double bmiChange,
        String bmiTrend,
        String healthGoal
) {}
