package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.CharityDashboardDTO;
import com.charityplatform.backend.dto.DonorDashboardDTO;
import com.charityplatform.backend.model.CampaignStatus;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.CampaignRepository;
import com.charityplatform.backend.repository.DonationRepository;
import com.charityplatform.backend.repository.VoteRepository;
import com.charityplatform.backend.repository.WithdrawalRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class DashboardService {

    private final DonationRepository donationRepository;
    private final VoteRepository voteRepository;
    private final CampaignRepository campaignRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    public DashboardService(DonationRepository donationRepository, VoteRepository voteRepository, CampaignRepository campaignRepository, WithdrawalRequestRepository withdrawalRequestRepository) {
        this.donationRepository = donationRepository;
        this.voteRepository = voteRepository;
        this.campaignRepository = campaignRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
    }

    @Transactional(readOnly = true)
    public DonorDashboardDTO getDonorDashboardData(User currentUser) {
        Long userId = currentUser.getId();
        BigDecimal totalDonated = donationRepository.sumAmountByUserId(userId);
        long campaignsSupported = donationRepository.countDistinctCampaignsByUserId(userId);
        long votesCast = voteRepository.countByVoterId(userId);

        DonorDashboardDTO dto = new DonorDashboardDTO();
        dto.setTotalAmountDonated(totalDonated);
        dto.setCampaignsSupported(campaignsSupported);
        dto.setVotesCast(votesCast);
        return dto;
    }

    @Transactional(readOnly = true)
    public CharityDashboardDTO getCharityDashboardData(User currentUser) {
        Charity charity = currentUser.getCharity();
        if (charity == null) {
            throw new AccessDeniedException("The current user is not associated with a charity.");
        }
        Long charityId = charity.getId();

        BigDecimal totalRaised = donationRepository.sumTotalDonationsByCharityId(charityId);
        long activeCampaigns = campaignRepository.countByCharityIdAndStatus(charityId, CampaignStatus.ACTIVE);
        long successfulCampaigns = campaignRepository.countByCharityIdAndStatus(charityId, CampaignStatus.COMPLETED);
        long pendingWithdrawals = withdrawalRequestRepository.countPendingByCharityId(charityId);

        CharityDashboardDTO dto = new CharityDashboardDTO();
        dto.setTotalRaisedAcrossAllCampaigns(totalRaised);
        dto.setActiveCampaigns(activeCampaigns);
        dto.setSuccessfulCampaigns(successfulCampaigns);
        dto.setPendingWithdrawalRequests(pendingWithdrawals);

        return dto;
    }
}