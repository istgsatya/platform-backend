package com.charityplatform.backend.dto;

import com.charityplatform.backend.model.RequestStatus;
import com.charityplatform.backend.model.WithdrawalRequest;
import java.math.BigDecimal;
import java.time.Instant;

public class WithdrawalResponseDTO {

    private Long id;
    private Long onChainRequestId;
    private BigDecimal amount;
    private String purpose;
    private String vendorAddress;
    private String financialProofUrl;
    private String visualProofUrl;
    private Instant votingDeadline;
    private RequestStatus status;
    private Instant createdAt;

    public static WithdrawalResponseDTO fromEntity(WithdrawalRequest request) {
        WithdrawalResponseDTO dto = new WithdrawalResponseDTO();
        dto.setId(request.getId());
        dto.setOnChainRequestId(request.getOnChainRequestId());
        dto.setAmount(request.getAmount());
        dto.setPurpose(request.getPurpose());
        dto.setVendorAddress(request.getVendorAddress());
        dto.setFinancialProofUrl(request.getFinancialProofUrl());
        dto.setVisualProofUrl(request.getVisualProofUrl());
        dto.setVotingDeadline(request.getVotingDeadline());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        return dto;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOnChainRequestId() { return onChainRequestId; }
    public void setOnChainRequestId(Long onChainRequestId) { this.onChainRequestId = onChainRequestId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getVendorAddress() { return vendorAddress; }
    public void setVendorAddress(String vendorAddress) { this.vendorAddress = vendorAddress; }
    public String getFinancialProofUrl() { return financialProofUrl; }
    public void setFinancialProofUrl(String financialProofUrl) { this.financialProofUrl = financialProofUrl; }
    public String getVisualProofUrl() { return visualProofUrl; }
    public void setVisualProofUrl(String visualProofUrl) { this.visualProofUrl = visualProofUrl; }
    public Instant getVotingDeadline() { return votingDeadline; }
    public void setVotingDeadline(Instant votingDeadline) { this.votingDeadline = votingDeadline; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}