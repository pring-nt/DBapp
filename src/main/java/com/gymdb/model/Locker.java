package com.gymdb.model;

import java.time.LocalDate;

public record Locker(
        int lockerID,
        String status,
        LocalDate rentalStartDate,
        LocalDate rentalEndDate
) {}
