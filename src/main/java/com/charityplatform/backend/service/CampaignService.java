package com.charityplatform.backend.service;

import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CampaignBalanceDTO;
import com.charityplatform.backend.dto.CampaignResponseDTO;
import com.charityplatform.backend.dto.CreateCampaignRequest;
import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.CampaignRepository;
import com.charityplatform.backend.repository.CharityRepository;
import com.charityplatform.backend.repository.WithdrawalRequestRepository; // <-- 1. NEW IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final PlatformLedger platformLedger;
    private final CharityRepository charityRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository; // <-- 2. NEW REPOSITORY

    @Autowired
    public CampaignService(CampaignRepository campaignRepository, PlatformLedger platformLedger,
                           CharityRepository charityRepository, WithdrawalRequestRepository withdrawalRequestRepository) { // <-- 3. CONSTRUCTOR UPDATED
        this.campaignRepository = campaignRepository;
        this.platformLedger = platformLedger;
        this.charityRepository = charityRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository; // <-- 3. CONSTRUCTOR UPDATED
    }


    @Transactional
    public Campaign createCampaign(CreateCampaignRequest request, User currentUser) {
        Charity detachedCharity = currentUser.getCharity();
        if (detachedCharity == null) { throw new IllegalStateException("User is not a charity admin and cannot create a campaign."); }
        Charity managedCharity = charityRepository.findById(detachedCharity.getId())
                .orElseThrow(() -> new RuntimeException("Could not find the charity associated with the current user."));
        Campaign campaign = new Campaign();
        campaign.setTitle(request.getTitle());
        campaign.setDescription(request.getDescription());
        campaign.setGoalAmount(request.getGoalAmount());
        campaign.setCharity(managedCharity);
        return campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public List<Campaign> getActiveCampaigns() {
        List<Campaign> campaigns = campaignRepository.findByStatusWithCharity(com.charityplatform.backend.model.CampaignStatus.ACTIVE);
        return campaigns;
    }


    @Transactional(readOnly = true)
    public Campaign getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findByIdWithCharity(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        return campaign;
    }

    // --- 4. THIS IS THE NEW METHOD TO USE FOR YOUR CAMPAIGN DETAIL PAGE ---
    @Transactional(readOnly = true)
    public CampaignResponseDTO getCampaignDetailsById(Long id) {
        Campaign campaign = campaignRepository.findByIdWithCharity(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));

        // Create the DTO from the entity
        CampaignResponseDTO dto = CampaignResponseDTO.fromCampaign(campaign);

        // Calculate total withdrawn amount
        BigDecimal totalWithdrawn = withdrawalRequestRepository.findTotalWithdrawnByCampaignId(id);

        // Set the value, defaulting to zero if null
        dto.setTotalWithdrawn(totalWithdrawn != null ? totalWithdrawn : BigDecimal.ZERO);

        return dto;
    }

    public CampaignBalanceDTO getCampaignBalance(Long campaignId) {
        try {
            BigInteger balanceInWei = platformLedger.campaignBalances(BigInteger.valueOf(campaignId)).send();
            BigDecimal balanceInEth = new BigDecimal(balanceInWei).divide(new BigDecimal("1E18"));
            return new CampaignBalanceDTO(balanceInEth);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch campaign balance from blockchain for campaign ID " + campaignId, e);
        }
    }


    @Transactional(readOnly = true)
    public List<CampaignResponseDTO> getCampaignsByCharity(Long charityId) {

        List<Campaign> campaigns = campaignRepository.findByCharityIdOrderByCreatedAtDesc(charityId);
        return campaigns.stream()
                .map(CampaignResponseDTO::fromCampaign)
                .collect(Collectors.toList());
    }

}