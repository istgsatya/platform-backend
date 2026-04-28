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
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
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
    private final RestTemplate restTemplate;
    @Value("${etherscan.api.key}")
    private String etherscanApiKey;

    @Autowired
    public DonationService(Web3j web3j, PlatformLedger platformLedger, DonationRepository donationRepository, CampaignRepository campaignRepository,RestTemplate restTemplate) {
        this.web3j = web3j;
        this.platformLedger = platformLedger;
        this.donationRepository = donationRepository;
        this.campaignRepository = campaignRepository;
        this.restTemplate = restTemplate;
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
        donation.setAmount(onChainAmountInEth);
        donation.setTransactionHash(request.getTransactionHash());
        donation.setPaymentMethod("CRYPTO");


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
    @Transactional(readOnly = true)
    public boolean hasUserDonatedToCampaign(Long campaignId, User currentUser) {
        if (currentUser == null) return false;
        return donationRepository.existsByCampaignIdAndUserId(campaignId, currentUser.getId());
    }


    public String getOwnerFromTransactionHash(String txHash) {

        // The fixed URL now correctly uses the 'txHash' variable from the method argument.
        // The chain ID, module, action, and API key remain hardcoded as you specified.
        String url = String.format(
                "https://api.etherscan.io/v2/api?chainid=11155111&module=proxy&action=eth_getTransactionReceipt&txhash=%s&apikey=6HH4RNGIHXMJ3G51V7HFKWSB9TQ9QA5DR6",
              //  "https://api-sepolia.etherscan.io/api?module=proxy&action=eth_getTransactionReceipt&txhash=%s&apikey=6HH4RNGIHXMJ3G51V7HFKWSB9TQ9QA5DR6",
                txHash
        );

        try {
            JsonNode root = restTemplate.getForObject(url, JsonNode.class);

            // All validation logic remains the same.
            if (root == null || !root.has("result") || root.get("result").isNull()) {
                throw new RuntimeException("Transaction receipt not found on Etherscan for hash: " + txHash);
            }

            JsonNode result = root.get("result");

            if (!result.has("from") || result.get("from").isNull()) {
                throw new RuntimeException("'from' address not found within the Etherscan transaction receipt response.");
            }

            if (!result.has("status") || !result.get("status").asText().equals("0x1")) {
                throw new RuntimeException("Transaction was found, but its on-chain execution status is FAILED. Hash: " + txHash);
            }

            return result.get("from").asText();

        } catch (Exception e) {
            throw new RuntimeException("A critical error occurred while verifying the transaction with Etherscan: " + e.getMessage(), e);
        }
    }
}