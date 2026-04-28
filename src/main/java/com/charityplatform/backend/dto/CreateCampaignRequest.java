package com.charityplatform.backend.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public class CreateCampaignRequest {

    @NotBlank(message = "Campaign title is required.")
    private String title;

    @NotBlank(message = "Campaign description is required.")
    private String description;

    @NotNull(message = "Goal amount is required.")
    @DecimalMin(value = "0.0001", message = "Goal amount must be at least 0.0001(eth).")
    private BigDecimal goalAmount;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getGoalAmount() {
        return goalAmount;
    }

    public void setGoalAmount(BigDecimal goalAmount) {
        this.goalAmount = goalAmount;
    }
}
