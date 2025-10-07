// src/main/java/com/charityplatform/backend/dto/CampaignResponseDTO.java
package com.charityplatform.backend.dto;

import com.charityplatform.backend.model.Campaign;
import java.math.BigDecimal;
import java.time.Instant;

public class CampaignResponseDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private String charityName;
    private Long charityId;
    private Instant createdAt;

    // Static factory method for easy conversion
    public static CampaignResponseDTO fromCampaign(Campaign campaign) {
        CampaignResponseDTO dto = new CampaignResponseDTO();
        dto.setId(campaign.getId());
        dto.setTitle(campaign.getTitle());
        dto.setDescription(campaign.getDescription());
        dto.setGoalAmount(campaign.getGoalAmount());
        dto.setCurrentAmount(campaign.getCurrentAmount());
        dto.setCreatedAt(campaign.getCreatedAt());

        // This is the key part that triggers the lazy loading safely
        if (campaign.getCharity() != null) {
            dto.setCharityName(campaign.getCharity().getName());
            dto.setCharityId(campaign.getCharity().getId());
        }

        return dto;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getGoalAmount() { return goalAmount; }
    public void setGoalAmount(BigDecimal goalAmount) { this.goalAmount = goalAmount; }
    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }
    public String getCharityName() { return charityName; }
    public void setCharityName(String charityName) { this.charityName = charityName; }
    public Long getCharityId() { return charityId; }
    public void setCharityId(Long charityId) { this.charityId = charityId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}