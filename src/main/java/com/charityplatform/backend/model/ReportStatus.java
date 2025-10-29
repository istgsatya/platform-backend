package com.charityplatform.backend.model;

public enum ReportStatus {
    PENDING,    // The initial state of a new report.
    VALIDATED,  // An admin has reviewed the report and confirmed it's valid.
    REJECTED    // An admin has reviewed the report and deemed it invalid.
}