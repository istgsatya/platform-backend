package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.ContentPostResponseDTO;
import com.charityplatform.backend.dto.CreatePostRequest;
import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.ContentPostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ContentPostController {

    private final ContentPostService contentPostService;

    @Autowired
    public ContentPostController(ContentPostService contentPostService) {
        this.contentPostService = contentPostService;
    }

    @PostMapping(value = "/posts", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')")
    public ResponseEntity<?> createPost(
            @Valid @RequestPart("post") CreatePostRequest request,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile,
            @AuthenticationPrincipal User currentUser) {
        try {
            // FIX: The service now returns a DTO.
            ContentPostResponseDTO newPostDto = contentPostService.createPost(request, mediaFile, currentUser);
            return new ResponseEntity<>(newPostDto, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/charities/{id}/posts")
    public ResponseEntity<List<ContentPostResponseDTO>> getPostsForCharity(@PathVariable("id") Long charityId) {
        // FIX: The service now returns a List of DTOs.
        List<ContentPostResponseDTO> postDtos = contentPostService.getPostsByCharity(charityId);
        return ResponseEntity.ok(postDtos);
    }
}