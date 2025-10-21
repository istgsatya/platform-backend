package com.charityplatform.backend.service;
/// worst fucking sevice ever
import com.charityplatform.backend.dto.CampaignResponseDTO;
import com.charityplatform.backend.dto.CreateCampaignRequest;
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.CampaignRepository;
import com.charityplatform.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    @Autowired
    public CampaignService(CampaignRepository campaignRepository, UserRepository userRepository) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new campaign for an authenticated charity admin.
     * Re-fetches the user to ensure all associations are managed within the transaction.
     * Converts the final saved entity to a DTO before returning to solve lazy loading on create.
     */
    @Transactional
    public CampaignResponseDTO createCampaign(CreateCampaignRequest request, User charityAdminPrincipal) {
        // Re-fetch the user to get a managed entity within this transaction
        User managedAdmin = userRepository.findById(charityAdminPrincipal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));

        Charity charity = managedAdmin.getCharity();

        // Perform security and business logic checks
        if (charity == null) {
            throw new IllegalStateException("User is not associated with any charity.");
        }
        if (charity.getStatus() != VerificationStatus.APPROVED) {
            throw new IllegalStateException("Charity is not approved to create campaigns.");
        }

        // Create and save the new campaign
        Campaign campaign = new Campaign();
        campaign.setTitle(request.getTitle());
        campaign.setDescription(request.getDescription());
        campaign.setGoalAmount(request.getGoalAmount());
        campaign.setCharity(charity);

        Campaign savedCampaign = campaignRepository.save(campaign);

        // Return the DTO, converted safely inside the transaction
        return CampaignResponseDTO.fromCampaign(savedCampaign);
    }

    /**
     * Fetches a list of all ACTIVE campaigns using an optimized JOIN FETCH query
     * to prevent LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public List<Campaign> getActiveCampaigns() {
        return campaignRepository.findByStatusWithCharity(CampaignStatus.ACTIVE);
    }

    /**
     * Fetches a single campaign by its ID using an optimized JOIN FETCH query
     * to prevent LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public Campaign getCampaignById(Long id) {
        return campaignRepository.findByIdWithCharity(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
    }
}