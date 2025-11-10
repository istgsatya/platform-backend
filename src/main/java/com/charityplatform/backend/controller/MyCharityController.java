package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.CampaignResponseDTO;
import com.charityplatform.backend.dto.DonationResponseDTO;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.CampaignService;
import com.charityplatform.backend.service.CharityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/my-charity") // <-- A BRAND NEW, UNAMBIGUOUS PATH
@PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')") // Secure the entire controller
public class MyCharityController {

    private final CampaignService campaignService;
    private final CharityService charityService;

    @Autowired
    public MyCharityController(CampaignService campaignService, CharityService charityService) {
        this.campaignService = campaignService;
        this.charityService = charityService;
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponseDTO>> getMyCharityCampaigns(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(campaignService.getCampaignsByCharity(currentUser.getCharity().getId()));
    }

    @GetMapping("/donations")
    public ResponseEntity<List<DonationResponseDTO>> getDonationsForMyCharity(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(charityService.getDonationsForMyCharity(currentUser));
    }
}