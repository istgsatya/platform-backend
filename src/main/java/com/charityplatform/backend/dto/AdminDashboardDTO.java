package com.charityplatform.backend.dto;

import java.math.BigDecimal;

public class AdminDashboardDTO {

    private long totalUsers;
    private long totalCharities;
    private long totalCampaigns;
    private long pendingCharityApplications;
    private long pendingReports;
    private BigDecimal totalDonationsValue;

   
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalCharities() { return totalCharities; }
    public void setTotalCharities(long totalCharities) { this.totalCharities = totalCharities; }
    public long getTotalCampaigns() { return totalCampaigns; }
    public void setTotalCampaigns(long totalCampaigns) { this.totalCampaigns = totalCampaigns; }
    public long getPendingCharityApplications() { return pendingCharityApplications; }
    public void setPendingCharityApplications(long pendingCharityApplications) { this.pendingCharityApplications = pendingCharityApplications; }
    public long getPendingReports() { return pendingReports; }
    public void setPendingReports(long pendingReports) { this.pendingReports = pendingReports; }
    public BigDecimal getTotalDonationsValue() { return totalDonationsValue; }
    public void setTotalDonationsValue(BigDecimal totalDonationsValue) { this.totalDonationsValue = totalDonationsValue; }
}