package com.charityplatform.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name= "charities")
public class Charity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max=255)
    @Column(unique = true,nullable = false)
    private String name;

    @NotBlank
    @Lob
    @Column(columnDefinition= "TEXT")
    private String description;


    private String registrationDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(nullable = false,updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // --- START: NEW FIELD ---
    @Column
    private Instant rffCooldownUntil; // Stores the timestamp until which a charity cannot create new RFFs
    // --- END: NEW FIELD ---

    @OneToOne(mappedBy ="charity")
    private User adminUser;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        status = VerificationStatus.PENDING;
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRegistrationDocumentUrl() {
        return registrationDocumentUrl;
    }

    public void setRegistrationDocumentUrl(String registrationDocumentUrl) {
        this.registrationDocumentUrl = registrationDocumentUrl;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- START: NEW GETTER/SETTER ---
    public Instant getRffCooldownUntil() {
        return rffCooldownUntil;
    }

    public void setRffCooldownUntil(Instant rffCooldownUntil) {
        this.rffCooldownUntil = rffCooldownUntil;
    }
    // --- END: NEW GETTER/SETTER ---
}