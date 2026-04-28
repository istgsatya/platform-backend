package com.charityplatform.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blacklisted_identifiers",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"identifierValue", "identifierType"}) })
public class BlacklistedIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String identifierValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentifierType identifierType;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdentifierValue() { return identifierValue; }
    public void setIdentifierValue(String identifierValue) { this.identifierValue = identifierValue; }
    public IdentifierType getIdentifierType() { return identifierType; }
    public void setIdentifierType(IdentifierType identifierType) { this.identifierType = identifierType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}