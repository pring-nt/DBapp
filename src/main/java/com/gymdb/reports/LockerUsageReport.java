package com.gymdb.reports;

public record LockerUsageReport(
        String category,
        int lockerCount,
        String percentOfTotal
) {}
