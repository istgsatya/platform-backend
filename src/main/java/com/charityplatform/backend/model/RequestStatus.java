package com.charityplatform.backend.model;

public enum RequestStatus {
    PENDING_VOTE,
    APPROVED,
    REJECTED,
    PENDING_ADMIN_REVIEW,
    EXECUTED,
    AWAITING_ADMIN_DECISION 
}