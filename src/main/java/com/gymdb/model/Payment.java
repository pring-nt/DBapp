package com.gymdb.model;

import java.sql.Timestamp;

public record Payment(
        int paymentID,
        String payment_num,
        Timestamp payment_date,
        String transaction_type,
        double amount,
        String payment_method,
        int memberID
) {}
