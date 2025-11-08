package com.gymdb.model;

import java.time.LocalDate;

public record Equipment(
        int id,
        String name,
        String description,
        int quantity,
        double amount,
        String vendor,
        String contact,
        LocalDate purchaseDate
) {}

