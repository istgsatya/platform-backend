package com.charityplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CryptoDonationRequest {

    @NotBlank(message = "Transaction hash cannot be blank")
    private String transactionHash;

    // --- Getters and Setters ---
    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }
}