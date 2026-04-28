package com.charityplatform.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reports")
// --- START: ADD THE ENTITY GRAPH ---
@NamedEntityGraph(
        name = "Report.withAllDetails",
        attributeNodes = {
                @NamedAttributeNode("reporter"),
                @NamedAttributeNode(value = "reportedRequest", subgraph = "reportedRequest-subgraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "reportedRequest-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode(value = "campaign", subgraph = "campaign-subgraph")
                        }
                ),
                @NamedSubgraph(
                        name = "campaign-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("charity")
                        }
                )
        }
)
// --- END: ADD THE ENTITY GRAPH ---
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter; // The user who filed the report

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_request_id", nullable = false)
    private WithdrawalRequest reportedRequest; // The RFF being reported

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason; // The user's justification for the report

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        status = ReportStatus.PENDING; // New reports are always pending review
    }

    // --- Getters and Setters (unchanged) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public WithdrawalRequest getReportedRequest() {
        return reportedRequest;
    }

    public void setReportedRequest(WithdrawalRequest reportedRequest) {
        this.reportedRequest = reportedRequest;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}