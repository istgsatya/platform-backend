package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.DonationResponseDTO;
import com.charityplatform.backend.dto.CryptoDonationRequest;
import com.charityplatform.backend.model.Donation; // No longer needed
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List; // <-- New Import

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;

    @Autowired
    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping("/crypto/verify")
    @PreAuthorize("hasAuthority('ROLE_DONOR') or hasAuthority('ROLE_CHARITY_ADMIN') or hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<DonationResponseDTO> verifyCryptoDonation(@Valid @RequestBody CryptoDonationRequest request,
                                                                    @AuthenticationPrincipal User currentUser) {
        // Service now returns a Donation entity, which we convert to DTO
        Donation savedDonation = donationService.verifyAndSaveCryptoDonation(request, currentUser);
        return ResponseEntity.ok(DonationResponseDTO.fromDonation(savedDonation));
    }

    // --- START: NEW HISTORY ENDPOINT ---
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<List<DonationResponseDTO>> getCurrentUserDonations(@AuthenticationPrincipal User currentUser) {
        List<DonationResponseDTO> donations = donationService.getDonationsForCurrentUser(currentUser);
        return ResponseEntity.ok(donations);
    }
    // --- END: NEW HISTORY ENDPOINT ---
}