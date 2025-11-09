package com.charityplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public class CharityApplicationRequest {

    @NotBlank(message = "Charity name is required.")
    @Size(max = 255, message = "Charity name cannot exceed 255 characters.")
    private String name;

    @NotBlank(message = "Description is required.")
    @Size(min = 50, message = "Description must be at least 50 characters long.")
    private String description;

    // --- START: NEW FIELD ---
    @NotBlank(message = "Payout wallet address is required.")
    @Length(min = 42, max = 42, message = "Wallet address must be 42 characters long.")
    private String payoutWalletAddress;
    // --- END: NEW FIELD ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --- START: NEW GETTER/SETTER ---
    public String getPayoutWalletAddress() {
        return payoutWalletAddress;
    }

    public void setPayoutWalletAddress(String payoutWalletAddress) {
        this.payoutWalletAddress = payoutWalletAddress;
    }
    // --- END: NEW GETTER/SETTER ---
}