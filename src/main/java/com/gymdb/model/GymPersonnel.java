package com.gymdb.model;

public record GymPersonnel(
        int personnelID,
        String firstName,
        String lastName,
        String personnelType,
        String schedule,
        String instructorRecord,
        String speciality
) {}
