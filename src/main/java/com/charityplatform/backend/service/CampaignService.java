package com.charityplatform.backend.service;

import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CampaignBalanceDTO;
import com.charityplatform.backend.dto.CreateCampaignRequest;
import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.CampaignRepository;
import com.charityplatform.backend.repository.CharityRepository; // <-- MAY NEED TO INJECT
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final PlatformLedger platformLedger;
    private final CharityRepository charityRepository; // For re-fetching

    @Autowired
    public CampaignService(CampaignRepository campaignRepository, PlatformLedger platformLedger, CharityRepository charityRepository) {
        this.campaignRepository = campaignRepository;
        this.platformLedger = platformLedger;
        this.charityRepository = charityRepository;
    }

    @Transactional
    public Campaign createCampaign(CreateCampaignRequest request, User currentUser) {
        // --- THIS IS THE FINAL EXORCISM ---
        // Get the detached charity proxy from the current user.
        Charity detachedCharity = currentUser.getCharity();
        if (detachedCharity == null) {
            throw new IllegalStateException("User is not a charity admin and cannot create a campaign.");
        }

        // RE-FETCH the charity from the database USING ITS ID. This gets us a live, "managed" entity.
        Charity managedCharity = charityRepository.findById(detachedCharity.getId())
                .orElseThrow(() -> new RuntimeException("Could not find the charity associated with the current user."));

        Campaign campaign = new Campaign();
        campaign.setTitle(request.getTitle());
        campaign.setDescription(request.getDescription());
        campaign.setGoalAmount(request.getGoalAmount());

        // We now set the LIVE, MANAGED charity object, not the dead proxy.
        campaign.setCharity(managedCharity);

        return campaignRepository.save(campaign);
        // --- END OF THE EXORCISM ---
    }

    @Transactional(readOnly = true)
    public List<Campaign> getActiveCampaigns() {
        List<Campaign> campaigns = campaignRepository.findByStatus(com.charityplatform.backend.model.CampaignStatus.ACTIVE);
        // Defensively initialize to be safe for any downstream DTO conversions.
        campaigns.forEach(campaign -> Hibernate.initialize(campaign.getCharity()));
        return campaigns;
    }

    @Transactional(readOnly = true)
    public Campaign getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        Hibernate.initialize(campaign.getCharity());
        return campaign;
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
}