package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.*;
import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.CampaignService;
import com.charityplatform.backend.service.DonationService;
import com.charityplatform.backend.service.WithdrawalService;
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
    private final WithdrawalService withdrawalService;
    private final DonationService donationService;

    @Autowired
    public CampaignController(CampaignService campaignService, WithdrawalService withdrawalService, DonationService donationService) {
        this.campaignService = campaignService;
        this.withdrawalService = withdrawalService;
        this.donationService = donationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')")
    public ResponseEntity<CampaignResponseDTO> createCampaign(@Valid @RequestBody CreateCampaignRequest request,
                                                              @AuthenticationPrincipal User currentUser) {
        // The service now correctly returns the raw Campaign entity.
        Campaign createdCampaign = campaignService.createCampaign(request, currentUser);
        // We correctly convert it to the DTO here in the controller. No more type mismatch.
        return ResponseEntity.ok(CampaignResponseDTO.fromCampaign(createdCampaign));
    }

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

    @GetMapping("/{id}/withdrawals")
    public ResponseEntity<List<WithdrawalResponseDTO>> getWithdrawalsForCampaign(@PathVariable Long id) {
        List<WithdrawalResponseDTO> withdrawals = withdrawalService.getWithdrawalsForCampaign(id);
        return ResponseEntity.ok(withdrawals);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<CampaignBalanceDTO> getCampaignBalance(@PathVariable Long id) {
        CampaignBalanceDTO balanceDto = campaignService.getCampaignBalance(id);
        return ResponseEntity.ok(balanceDto);
    }

    @GetMapping("/{id}/donations")
    public ResponseEntity<List<DonationResponseDTO>> getDonationsForCampaign(@PathVariable Long id) {
        List<DonationResponseDTO> donations = donationService.getDonationsForCampaign(id);
        return ResponseEntity.ok(donations);
    }
}