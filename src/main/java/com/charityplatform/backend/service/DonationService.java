package com.charityplatform.backend.service;

import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CryptoDonationRequest;
import com.charityplatform.backend.dto.DonationResponseDTO;
import com.charityplatform.backend.dto.OnChainDonationInfo;
import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.Donation;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.CampaignRepository;
import com.charityplatform.backend.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert; // <-- New Import

import java.io.IOException;
import java.math.BigDecimal; // <-- New Import
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonationService {
    private final Web3j web3j;
    private final PlatformLedger platformLedger;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;

    @Autowired
    public DonationService(Web3j web3j, PlatformLedger platformLedger, DonationRepository donationRepository, CampaignRepository campaignRepository) {
        this.web3j = web3j;
        this.platformLedger = platformLedger;
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
    }

    @Transactional
    public Donation verifyAndSaveCryptoDonation(CryptoDonationRequest request, User user){
        if(donationRepository.existsByTransactionHash(request.getTransactionHash())){
            throw new IllegalStateException("This transaction has already been processed.");
        }

        TransactionReceipt receipt;
        try {
            receipt = web3j.ethGetTransactionReceipt(request.getTransactionHash()).send().getTransactionReceipt()
                    .orElseThrow(() -> new RuntimeException("Transaction receipt not found."));
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to blockchain node.", e);
        }

        List<PlatformLedger.DonationRecordedEventResponse> events = platformLedger.getDonationRecordedEvents(receipt);
        if (events.isEmpty()) {
            throw new IllegalStateException("No valid donation event found in this transaction.");
        }

        PlatformLedger.DonationRecordedEventResponse event = events.get(0);

        // --- START: THE FINAL, CORRECTED LOGIC ---

        // 1. We get the Campaign ID DIRECTLY from the on-chain event. This is the source of truth.
        Long onChainCampaignId = event.campaignId.longValue();

        // 2. We use this trusted ID to find our campaign entity.
        Campaign campaign = campaignRepository.findById(onChainCampaignId)
                .orElseThrow(() -> new RuntimeException("Campaign with on-chain ID " + onChainCampaignId + " not found in our database."));

        // 3. We also get the donation amount DIRECTLY from the on-chain event, not the client request.
        BigDecimal onChainAmountInEth = Convert.fromWei(event.amount.toString(), Convert.Unit.ETHER);

        // --- END: THE FINAL, CORRECTED LOGIC ---

        Donation donation = new Donation();
        donation.setUser(user);
        donation.setCampaign(campaign);
        donation.setAmount(onChainAmountInEth); // Use the trusted, on-chain amount
        donation.setTransactionHash(request.getTransactionHash());
        donation.setPaymentMethod("CRYPTO");

        // Here we can finally update the stale database amount as a bonus
        BigDecimal newTotal = campaign.getCurrentAmount().add(onChainAmountInEth);
        campaign.setCurrentAmount(newTotal);
        campaignRepository.save(campaign);

        return donationRepository.save(donation);
    }

    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsForCurrentUser(User currentUser) {
        List<Donation> donations = donationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return donations.stream()
                .map(DonationResponseDTO::fromDonation)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsForCampaign(Long campaignId) {
        List<Donation> donations = donationRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId);
        return donations.stream()
                .map(DonationResponseDTO::fromDonation)
                .collect(Collectors.toList());
    }
}