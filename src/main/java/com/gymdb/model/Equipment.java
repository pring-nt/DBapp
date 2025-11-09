package com.gymdb.model;

import java.time.LocalDateTime;

public record Equipment(
        int equipmentID,
        String equipmentName,
        String equipmentDescription,
        int quantity,
        double unitPrice,
        String vendor,
        String contactNo,
        LocalDateTime purchaseDate
) {}
