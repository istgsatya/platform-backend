package com.charityplatform.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.charityplatform.backend.model.RequestStatus;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name="campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

   // private RequestStatus status;
//
   //  public void setStatus(RequestStatus status) {
  //      this.status = status;
 //   }

    @NotNull
    @Positive
    @Column(precision = 19, scale = 4)
    private BigDecimal goalAmount;

    @NotNull
    @Column(precision = 19, scale = 4)
    private BigDecimal currentAmount;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="charity_id",nullable = false)
    private Charity charity;


    @Column(nullable=false,updatable = false)
    private Instant createdAt;
    private Instant endDate;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        status = CampaignStatus.ACTIVE;
        currentAmount = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public Charity getCharity() {
        return charity;
    }

    public void setCharity(Charity charity) {
        this.charity = charity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }
}
