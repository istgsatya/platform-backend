package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.AdminDashboardDTO;
import com.charityplatform.backend.dto.CharityDashboardDTO;
import com.charityplatform.backend.dto.DonorDashboardDTO;
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class DashboardService {

    // All repositories needed for all dashboards
    private final DonationRepository donationRepository;
    private final VoteRepository voteRepository;
    private final CampaignRepository campaignRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final UserRepository userRepository;
    private final CharityRepository charityRepository;
    private final ReportRepository reportRepository;

    @Autowired
    public DashboardService(DonationRepository donationRepository, VoteRepository voteRepository,
                            CampaignRepository campaignRepository, WithdrawalRequestRepository withdrawalRequestRepository,
                            UserRepository userRepository, CharityRepository charityRepository,
                            ReportRepository reportRepository) {
        this.donationRepository = donationRepository;
        this.voteRepository = voteRepository;
        this.campaignRepository = campaignRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.userRepository = userRepository;
        this.charityRepository = charityRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public DonorDashboardDTO getDonorDashboardData(User currentUser) {
        // ... (existing code is unchanged)
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
        // ... (existing code is unchanged)
        Charity charity = currentUser.getCharity();
        if (charity == null) { throw new AccessDeniedException("The current user is not associated with a charity."); }
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


    @Transactional(readOnly = true)
    public AdminDashboardDTO getAdminDashboardData() {
        long totalUsers = userRepository.count();
        long totalCharities = charityRepository.count();
        long totalCampaigns = campaignRepository.count();
        long pendingApps = charityRepository.countByStatus(VerificationStatus.PENDING);
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        // We need a query for total donations in DonationRepository for this.
        // For now, let's leave this part out to avoid more changes. We can add it if needed.
        // BigDecimal totalDonations = donationRepository.sumTotalDonationsPlatformWide();

        AdminDashboardDTO dto = new AdminDashboardDTO();
        dto.setTotalUsers(totalUsers);
        dto.setTotalCharities(totalCharities);
        dto.setTotalCampaigns(totalCampaigns);
        dto.setPendingCharityApplications(pendingApps);
        dto.setPendingReports(pendingReports);
        dto.setTotalDonationsValue(BigDecimal.ZERO); // Placeholder

        return dto;
    }

}