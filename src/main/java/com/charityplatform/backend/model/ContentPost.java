package com.charityplatform.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "content_posts")
/// /hope the motherfucking lazintiallizngexception is gone fuck you hibernate bithc stip closing the session
@NamedEntityGraph(
        name = "ContentPost.withCharity",
        attributeNodes = @NamedAttributeNode("charity")
)
public class ContentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charity_id", nullable = false)
    private Charity charity; // The charity that made the post

    @Column(columnDefinition = "TEXT", nullable = false)
    private String textContent; // The body of the post

    @Column
    private String mediaUrl; // An optional URL to an image or video

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Charity getCharity() {
        return charity;
    }

    public void setCharity(Charity charity) {
        this.charity = charity;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}