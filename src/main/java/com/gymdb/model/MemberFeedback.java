package com.gymdb.model;

public record MemberFeedback(
        int feedbackID,
        String comments,
        int personnelID,
        int memberID
) {}
