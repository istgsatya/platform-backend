package com.charityplatform.backend.dto;




import jakarta.validation.constraints.NotNull;

public class VoteRequest {
    @NotNull(message = "Approval status cannot be null")
    private Boolean approve;

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }
}
