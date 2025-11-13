package com.charityplatform.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User voter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "withdrawal_request_id", nullable = false)
    private WithdrawalRequest withdrawalRequest;

    @Column(nullable = false)
    private boolean approved;

    @Column(nullable = true, unique = true) // Set to true if you want to ensure no duplicate hashes are ever saved
    private String transactionHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public User getVoter() {
        return voter;
    }

    public void setVoter(User voter) {
        this.voter = voter;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public WithdrawalRequest getWithdrawalRequest() {
        return withdrawalRequest;
    }

    public void setWithdrawalRequest(WithdrawalRequest withdrawalRequest) {
        this.withdrawalRequest = withdrawalRequest;
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

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}