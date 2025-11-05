package com.gymdb.model;

import java.time.LocalDate;

public record Member(
        int memberID,
        String firstName,
        String lastName,
        String email,
        String contactNo,
        String membershipType,
        LocalDate startDate,
        LocalDate endDate,
        String healthGoal,
        Double initialWeight,
        Double goalWeight,
        Double startBMI,
        Double updatedBMI,
        Integer classID,
        Integer trainerID,
        Integer lockerID
) {}
