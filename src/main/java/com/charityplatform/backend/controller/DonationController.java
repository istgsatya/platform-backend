package com.charityplatform.backend.controller;






import com.charityplatform.backend.dto.DonationResponseDTO;
import com.charityplatform.backend.dto.CryptoDonationRequest;
import com.charityplatform.backend.model.Donation;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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
}
