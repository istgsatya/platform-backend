package com.charityplatform.backend.dto;

import com.charityplatform.backend.model.Donation;
import java.math.BigDecimal;
import java.time.Instant;

public class DonationResponseDTO {

    private Long id;
    private BigDecimal amount;
    private String transactionHash;
    private String paymentMethod;
    private Long campaignId;
    private String campaignTitle;
    private String username;

    public static DonationResponseDTO fromDonation(Donation donation) {
        DonationResponseDTO dto = new DonationResponseDTO();
        dto.setId(donation.getId());
        dto.setAmount(donation.getAmount());
        dto.setTransactionHash(donation.getTransactionHash());
        dto.setPaymentMethod(donation.getPaymentMethod());

        if (donation.getCampaign() != null) {
            dto.setCampaignId(donation.getCampaign().getId());
            dto.setCampaignTitle(donation.getCampaign().getTitle());
        }

        if (donation.getUser() != null) {
            dto.setUsername(donation.getUser().getUsername());
        }

        return dto;
    }

    // --- Getters and Setters ---
    // (Generate them or write them manually)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public String getCampaignTitle() { return campaignTitle; }
    public void setCampaignTitle(String campaignTitle) { this.campaignTitle = campaignTitle; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}