package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.DonorDashboardDTO;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.DonationRepository;
import com.charityplatform.backend.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class DashboardService {

    private final DonationRepository donationRepository;
    private final VoteRepository voteRepository;

    @Autowired
    public DashboardService(DonationRepository donationRepository, VoteRepository voteRepository) {
        this.donationRepository = donationRepository;
        this.voteRepository = voteRepository;
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
}