package com.gymdb.model;

import java.time.LocalDateTime;

public record Purchase(
        int purchaseID,
        LocalDateTime purchaseDate,
        int quantity,
        int memberID,
        int productID
) {}
