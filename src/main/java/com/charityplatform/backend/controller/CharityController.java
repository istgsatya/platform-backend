package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.*;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.VerificationStatus;
import com.charityplatform.backend.service.CampaignService;
import com.charityplatform.backend.service.CharityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/charities")
public class CharityController {

    private static final Logger log = LoggerFactory.getLogger(CharityController.class);

    private final CharityService charityService;
    private final CampaignService campaignService;

    @Autowired
    public CharityController(CharityService charityService, CampaignService campaignService) {
        this.charityService = charityService;
        this.campaignService = campaignService;
    }

    // --- THIS IS THE NEW, CORRECTED APPLICATION ENDPOINT ---
    @PostMapping(value = "/apply", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<?> applyForCharityVerification(
            @RequestPart("applicationData") CharityApplicationRequest request,
            @RequestPart("registrationDocument") MultipartFile document,
            @AuthenticationPrincipal User currentUser) {

        log.info("CONTROLLER_HIT: /apply received for new charity '{}' from user '{}'.", request.getName(), currentUser.getUsername());
        try {
            Charity savedCharity = charityService.applyForVerification(request, document, currentUser);
            String message = String.format("Charity application for '%s' submitted successfully. Awaiting admin review.", savedCharity.getName());
            return ResponseEntity.ok(new MessageResponse(true, message));
        } catch (Exception e) {
            log.error("CHARITY_APPLICATION_FAILED: User '{}' failed to apply. Error: {}", currentUser.getUsername(), e.getMessage(), e);
            return new ResponseEntity<>(
                    new MessageResponse(false, "Application failed: " + e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // --- ALL OTHER METHODS REMAIN UNCHANGED ---

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

    @GetMapping("/{id}/campaigns")
    public ResponseEntity<List<CampaignResponseDTO>> getCampaignsForCharity(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignsByCharity(id));
    }

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
}