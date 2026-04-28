package com.charityplatform.backend.dto;
import java.math.BigDecimal;

public class CampaignBalanceDTO {
    private BigDecimal balance;

    public CampaignBalanceDTO(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}