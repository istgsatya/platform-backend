// PASTE THIS ENTIRE FILE CONTENT into CharityController.java

package com.charityplatform.backend.controller;
import com.charityplatform.backend.dto.CharityResponseDTO;

import com.charityplatform.backend.dto.CharityApplicationRequest;
import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.VerificationStatus;
import com.charityplatform.backend.service.CharityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
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

    // --- User-facing endpoint ---
    @PostMapping(value = "/apply", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<?> applyForCharityVerification(
            @RequestPart("application") CharityApplicationRequest request,
            @RequestPart("document") MultipartFile document,
            @AuthenticationPrincipal User currentUser) {

        charityService.applyForVerification(request, document, currentUser);

        return ResponseEntity.ok(new MessageResponse(true, "Charity application submitted successfully. Awaiting admin review."));
    }

    // --- Admin-facing endpoints ---
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<List<Charity>> getAllCharities(@RequestParam(required = false) VerificationStatus status) {
        List<Charity> charities = charityService.getCharitiesByStatus(status);
        return ResponseEntity.ok(charities);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<Charity> approveCharity(@PathVariable Long id) {
        Charity approvedCharity = charityService.approveCharity(id);
        return ResponseEntity.ok(approvedCharity);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<Charity> rejectCharity(@PathVariable Long id) {
        Charity rejectedCharity = charityService.rejectCharity(id);
        return ResponseEntity.ok(rejectedCharity);
    }

    @GetMapping("/approved")
    public ResponseEntity<CharityResponseDTO> getPublicCharityProfile(@PathVariable Long id){
        try{
            return ResponseEntity.ok(charityService.getApproveCharityById(id));
        }
        catch(RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }



}