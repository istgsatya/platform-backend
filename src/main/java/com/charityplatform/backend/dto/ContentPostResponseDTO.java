package com.charityplatform.backend.dto;

import com.charityplatform.backend.model.ContentPost;
import java.time.Instant;

public class ContentPostResponseDTO {

    private Long id;
    private String textContent;
    private String mediaUrl;
    private Instant createdAt;
    private Long charityId;
    private String charityName;

    public static ContentPostResponseDTO fromEntity(ContentPost post) {
        ContentPostResponseDTO dto = new ContentPostResponseDTO();
        dto.setId(post.getId());
        dto.setTextContent(post.getTextContent());
        dto.setMediaUrl(post.getMediaUrl());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setCharityId(post.getCharity().getId());
        dto.setCharityName(post.getCharity().getName());
        return dto;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Long getCharityId() { return charityId; }
    public void setCharityId(Long charityId) { this.charityId = charityId; }
    public String getCharityName() { return charityName; }
    public void setCharityName(String charityName) { this.charityName = charityName; }
}