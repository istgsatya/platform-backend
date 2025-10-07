package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.CampaignResponseDTO;
import com.charityplatform.backend.dto.CreateCampaignRequest;
import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.CampaignService;
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

    @Autowired
    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // --- Protected endpoint for charity admins ---
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')")
    public ResponseEntity<CampaignResponseDTO> createCampaign(@Valid @RequestBody CreateCampaignRequest request,
                                                              @AuthenticationPrincipal User currentUser) {
        // The service now directly returns the DTO, solving the lazy-loading issue.
        CampaignResponseDTO newCampaignDTO = campaignService.createCampaign(request, currentUser);
        return ResponseEntity.ok(newCampaignDTO);
    }

    // --- Public endpoints for browsing ---
    @GetMapping
    public ResponseEntity<List<CampaignResponseDTO>> getAllActiveCampaigns() {
        // We get entities from the service...
        List<Campaign> campaigns = campaignService.getActiveCampaigns();

        // ...and convert them to DTOs here in the controller.
        List<CampaignResponseDTO> responseDTOs = campaigns.stream()
                .map(CampaignResponseDTO::fromCampaign)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponseDTO> getCampaignById(@PathVariable Long id) {
        // We get the entity from the service...
        Campaign campaign = campaignService.getCampaignById(id);

        // ...and convert it to a DTO here in the controller.
        return ResponseEntity.ok(CampaignResponseDTO.fromCampaign(campaign));
    }
}