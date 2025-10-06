// FINAL CharityController.java
package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.CharityApplicationRequest;
import com.charityplatform.backend.dto.CharityResponseDTO;
import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.VerificationStatus;
import com.charityplatform.backend.service.CharityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/charities")
public class CharityController {

    private final CharityService charityService;

    @Autowired
    public CharityController(CharityService charityService) {
        this.charityService = charityService;
    }

    // --- User-facing endpoint for applying ---
    @PostMapping(value = "/apply", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<?> applyForCharityVerification(@RequestPart("application") CharityApplicationRequest request, @RequestPart("document") MultipartFile document, @AuthenticationPrincipal User currentUser) {
        charityService.applyForVerification(request, document, currentUser);
        return ResponseEntity.ok(new MessageResponse(true, "Charity application submitted successfully. Awaiting admin review."));
    }

    // --- Admin-facing endpoints for management ---
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<List<Charity>> getAllCharities(@RequestParam(required = false) VerificationStatus status) {
        return ResponseEntity.ok(charityService.getCharitiesByStatus(status));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<Charity> approveCharity(@PathVariable Long id) {
        return ResponseEntity.ok(charityService.approveCharity(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<Charity> rejectCharity(@PathVariable Long id) {
        return ResponseEntity.ok(charityService.rejectCharity(id));
    }

    // --- Public endpoints for donors to view ---
    @GetMapping("/approved")
    public ResponseEntity<List<CharityResponseDTO>> getApprovedCharities() {
        return ResponseEntity.ok(charityService.getApprovedCharities());
    }

    @GetMapping("/{id}/public")
    public ResponseEntity<CharityResponseDTO> getPublicCharityProfile(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(charityService.getApprovedCharityById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}