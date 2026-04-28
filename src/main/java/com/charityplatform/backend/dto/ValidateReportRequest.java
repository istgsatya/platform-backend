package com.charityplatform.backend.dto;

import jakarta.validation.constraints.NotNull;

public class ValidateReportRequest {

    @NotNull(message = "Validation decision cannot be null")
    private Boolean isValid;

    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }
}