package com.charityplatform.backend.dto;

import java.math.BigDecimal;

public class CharityDashboardDTO {

    private BigDecimal totalRaisedAcrossAllCampaigns;
    private long activeCampaigns;
    private long successfulCampaigns; // Stretch goal, but good to have
    private long pendingWithdrawalRequests;

    // --- Getters and Setters ---
    public BigDecimal getTotalRaisedAcrossAllCampaigns() { return totalRaisedAcrossAllCampaigns; }
    public void setTotalRaisedAcrossAllCampaigns(BigDecimal totalRaisedAcrossAllCampaigns) { this.totalRaisedAcrossAllCampaigns = totalRaisedAcrossAllCampaigns; }
    public long getActiveCampaigns() { return activeCampaigns; }
    public void setActiveCampaigns(long activeCampaigns) { this.activeCampaigns = activeCampaigns; }
    public long getSuccessfulCampaigns() { return successfulCampaigns; }
    public void setSuccessfulCampaigns(long successfulCampaigns) { this.successfulCampaigns = successfulCampaigns; }
    public long getPendingWithdrawalRequests() { return pendingWithdrawalRequests; }
    public void setPendingWithdrawalRequests(long pendingWithdrawalRequests) { this.pendingWithdrawalRequests = pendingWithdrawalRequests; }
}