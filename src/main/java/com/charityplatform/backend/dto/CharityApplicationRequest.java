package com.charityplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL; // Import for URL validation

public class CharityApplicationRequest {

    @NotBlank(message = "Charity name is required.")
    @Size(max = 255, message = "Charity name cannot exceed 255 characters.")
    private String name;

    @NotBlank(message = "Description is required.")
    @Size(min = 50, message = "Description must be at least 50 characters long.")
    private String description;

    @NotBlank(message = "Payout wallet address is required.")
    @Length(min = 42, max = 42, message = "Wallet address must be 42 characters long.")
    private String payoutWalletAddress;

    // --- NEW FIELD FOR THE BANNER ---
    @NotBlank(message = "Banner Image IPFS Link is required.")
    @Size(min = 46, max = 59, message = "A valid IPFS CID is typically between 46 and 59 characters.")
    private String bannerImageUrl;
    // --- END: NEW FIELD ---


    // --- Getters and Setters ---

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

    public String getPayoutWalletAddress() {
        return payoutWalletAddress;
    }

    public void setPayoutWalletAddress(String payoutWalletAddress) {
        this.payoutWalletAddress = payoutWalletAddress;
    }

    // --- NEW GETTER/SETTER FOR THE BANNER ---
    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }
    // --- END: NEW GETTER/SETTER ---
}