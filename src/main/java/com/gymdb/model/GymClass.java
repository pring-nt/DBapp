package com.gymdb.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record GymClass(
        int classID,
        String className,
        String classType,
        LocalDate scheduleDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer personnelID
) {}