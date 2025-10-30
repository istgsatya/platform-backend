package com.charityplatform.backend.service;


import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CryptoDonationRequest;
import com.charityplatform.backend.dto.DonationResponseDTO; // <-- New Import
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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors; // <-- New Import

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
    public Donation verifyAndSaveCryptoDonation(CryptoDonationRequest request,User user){
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
        OnChainDonationInfo onChainInfo = new OnChainDonationInfo(event.campaignId, event.donor, event.amount);

        if (onChainInfo.campaignId().longValue() != request.getCampaignId()) {
            throw new IllegalStateException("On-chain campaign ID does not match request.");
        }
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found."));

        Donation donation = new Donation();
        donation.setUser(user);
        donation.setCampaign(campaign);
        donation.setAmount(request.getAmount());
        donation.setTransactionHash(request.getTransactionHash());
        donation.setPaymentMethod("CRYPTO");

        // You should really update the campaign's currentAmount here

        return donationRepository.save(donation);
    }

    // --- START: NEW HISTORY METHOD ---
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsForCurrentUser(User currentUser) {
        // Fetch the raw donation entities for the current user, newest first.
        // We will add an @EntityGraph here in a moment in the repository.
        List<Donation> donations = donationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        // Convert the list of entities to a list of safe DTOs.
        return donations.stream()
                .map(DonationResponseDTO::fromDonation)
                .collect(Collectors.toList());
    }
    // --- END: NEW HISTORY METHOD ---
}