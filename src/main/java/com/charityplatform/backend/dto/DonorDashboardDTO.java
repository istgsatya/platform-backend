package com.charityplatform.backend.dto;

import java.math.BigDecimal;

public class DonorDashboardDTO {

    private BigDecimal totalAmountDonated;
    private long campaignsSupported;
    private long votesCast;

    public BigDecimal getTotalAmountDonated() {
        return totalAmountDonated;
    }

    public void setTotalAmountDonated(BigDecimal totalAmountDonated) {
        this.totalAmountDonated = totalAmountDonated;
    }

    public long getVotesCast() {
        return votesCast;
    }

    public void setVotesCast(long votesCast) {
        this.votesCast = votesCast;
    }

    public long getCampaignsSupported() {
        return campaignsSupported;
    }

    public void setCampaignsSupported(long campaignsSupported) {
        this.campaignsSupported = campaignsSupported;
    }
}