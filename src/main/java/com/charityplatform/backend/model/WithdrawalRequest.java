package com.charityplatform.backend.model;


import com.charityplatform.backend.model.RequestStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "withdrawal_requests")
public class WithdrawalRequest {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is the ID of the request on the smart contract
    @Column(unique = true)
    private Long onChainRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    private String purpose;

    private String vendorAddress;

    private String financialProofUrl;

    private String visualProofUrl;

    private Instant votingDeadline;


    @Enumerated(EnumType.STRING)
    private RequestStatus status;;; // You'll need to move the RequestStatus enum to its own file

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        status = RequestStatus.PENDING_VOTE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getFinancialProofUrl() {
        return financialProofUrl;
    }

    public void setFinancialProofUrl(String financialProofUrl) {
        this.financialProofUrl = financialProofUrl;
    }

    public String getVendorAddress() {
        return vendorAddress;
    }

    public void setVendorAddress(String vendorAddress) {
        this.vendorAddress = vendorAddress;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Long getOnChainRequestId() {
        return onChainRequestId;
    }

    public void setOnChainRequestId(Long onChainRequestId) {
        this.onChainRequestId = onChainRequestId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getVotingDeadline() {
        return votingDeadline;
    }

    public void setVotingDeadline(Instant votingDeadline) {
        this.votingDeadline = votingDeadline;
    }

    public String getVisualProofUrl() {
        return visualProofUrl;
    }

    public void setVisualProofUrl(String visualProofUrl) {
        this.visualProofUrl = visualProofUrl;
    }
}
