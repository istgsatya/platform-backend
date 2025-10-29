package com.charityplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateReportRequest {

    @NotNull(message = "Reported request ID cannot be null")
    private Long reportedRequestId;

    @NotBlank(message = "Reason for reporting cannot be blank")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;

    // --- Getters and Setters ---

    public Long getReportedRequestId() {
        return reportedRequestId;
    }

    public void setReportedRequestId(Long reportedRequestId) {
        this.reportedRequestId = reportedRequestId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}