package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.ContentPostResponseDTO;
import com.charityplatform.backend.dto.CreatePostRequest;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.ContentPost;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.CharityRepository;
import com.charityplatform.backend.repository.ContentPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentPostService {

    private final ContentPostRepository contentPostRepository;
    private final FileStorageService fileStorageService;
    private final CharityRepository charityRepository;

    @Autowired
    public ContentPostService(ContentPostRepository contentPostRepository, FileStorageService fileStorageService, CharityRepository charityRepository) {
        this.contentPostRepository = contentPostRepository;
        this.fileStorageService = fileStorageService;
        this.charityRepository = charityRepository; // <-- INITIALIZE IT
    }

    @Transactional
    public ContentPostResponseDTO createPost(CreatePostRequest request, MultipartFile mediaFile, User currentUser) {
        // --- START: THE REAL FIX ---
        // 1. Get the potentially detached charity from the security context user.
        Charity userCharity = currentUser.getCharity();
        if (userCharity == null) {
            throw new AccessDeniedException("You are not associated with any charity.");
        }

        // 2. Re-fetch the Charity from the DB using its ID. This guarantees `managedCharity`
        // is a live, session-attached entity, not a dead proxy.
        Charity managedCharity = charityRepository.findById(userCharity.getId())
                .orElseThrow(() -> new RuntimeException("Associated charity with ID " + userCharity.getId() + " not found in database."));

        // 3. Perform the security check using the live entity.
        if (managedCharity.getAdminUser() == null || !managedCharity.getAdminUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You must be an admin of this charity to create a post.");
        }
        // --- END: THE REAL FIX ---

        String mediaFileUrl = null;
        if (mediaFile != null && !mediaFile.isEmpty()) {
            mediaFileUrl = fileStorageService.storeFile(mediaFile);
        }

        ContentPost newPost = new ContentPost();
        newPost.setCharity(managedCharity); // Use the live, managed charity object
        newPost.setTextContent(request.getTextContent());
        newPost.setMediaUrl(mediaFileUrl);

        ContentPost savedPost = contentPostRepository.save(newPost);

        // This will now work because the Charity object within savedPost is fully loaded.
        return ContentPostResponseDTO.fromEntity(savedPost);
    }

    @Transactional(readOnly = true)
    public List<ContentPostResponseDTO> getPostsByCharity(Long charityId) {
        List<ContentPost> posts = contentPostRepository.findByCharityIdOrderByCreatedAtDesc(charityId);
        return posts.stream()
                .map(ContentPostResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}