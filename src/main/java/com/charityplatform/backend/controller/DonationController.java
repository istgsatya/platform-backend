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
import java.util.Map;

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

        Donation savedDonation = donationService.verifyAndSaveCryptoDonation(request, currentUser);
        return ResponseEntity.ok(DonationResponseDTO.fromDonation(savedDonation));
    }


    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<List<DonationResponseDTO>> getCurrentUserDonations(@AuthenticationPrincipal User currentUser) {
        List<DonationResponseDTO> donations = donationService.getDonationsForCurrentUser(currentUser);
        return ResponseEntity.ok(donations);
    }

    @GetMapping("/has-donated/{campaignId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> hasDonated(@PathVariable Long campaignId, @AuthenticationPrincipal User currentUser) {
        boolean hasDonated = donationService.hasUserDonatedToCampaign(campaignId, currentUser);


        return ResponseEntity.ok(Map.of("hasDonated", hasDonated));
    }



    @GetMapping("/owner/{txHash}")
    public ResponseEntity<?> getTransactionOwner(@PathVariable String txHash) {
        try {

            String ownerAddress = donationService.getOwnerFromTransactionHash(txHash);

            // We return ONLY that one thing, in a clean JSON object.
            return ResponseEntity.ok(Map.of("ownerAddress", ownerAddress));
        } catch (RuntimeException e) {
            // If the hash is not found or Etherscan fails, return a 404 with the error.
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


}