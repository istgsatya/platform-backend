package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.CampaignResponseDTO;
import com.charityplatform.backend.dto.CreateCampaignRequest;
import com.charityplatform.backend.dto.WithdrawalResponseDTO; // <-- New Import
import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.CampaignService;
import com.charityplatform.backend.service.WithdrawalService; // <-- New Import
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final WithdrawalService withdrawalService; // <-- New Dependency

    @Autowired
    public CampaignController(CampaignService campaignService, WithdrawalService withdrawalService) {
        this.campaignService = campaignService;
        this.withdrawalService = withdrawalService; // <-- Initialize Dependency
    }

    // --- Protected endpoint for charity admins ---
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')")
    public ResponseEntity<CampaignResponseDTO> createCampaign(@Valid @RequestBody CreateCampaignRequest request,
                                                              @AuthenticationPrincipal User currentUser) {
        CampaignResponseDTO newCampaignDTO = campaignService.createCampaign(request, currentUser);
        return ResponseEntity.ok(newCampaignDTO);
    }

    // --- Public endpoints for browsing ---
    @GetMapping
    public ResponseEntity<List<CampaignResponseDTO>> getAllActiveCampaigns() {
        List<Campaign> campaigns = campaignService.getActiveCampaigns();
        List<CampaignResponseDTO> responseDTOs = campaigns.stream()
                .map(CampaignResponseDTO::fromCampaign)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponseDTO> getCampaignById(@PathVariable Long id) {
        Campaign campaign = campaignService.getCampaignById(id);
        return ResponseEntity.ok(CampaignResponseDTO.fromCampaign(campaign));
    }

    // --- START: NEW PUBLIC HISTORY ENDPOINT ---
    @GetMapping("/{id}/withdrawals")
    public ResponseEntity<List<WithdrawalResponseDTO>> getWithdrawalsForCampaign(@PathVariable Long id) {
        // The service does all the work, fetching and converting to DTOs.
        List<WithdrawalResponseDTO> withdrawals = withdrawalService.getWithdrawalsForCampaign(id);
        return ResponseEntity.ok(withdrawals);
    }
    // --- END: NEW PUBLIC HISTORY ENDPOINT ---
}