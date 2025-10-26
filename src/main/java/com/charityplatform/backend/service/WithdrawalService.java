package com.charityplatform.backend.service;

import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CreateWithdrawalRequest;
import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.WithdrawalRequest;
import com.charityplatform.backend.repository.CampaignRepository;
import com.charityplatform.backend.repository.UserRepository; // <-- ADD IMPORT
import com.charityplatform.backend.repository.WithdrawalRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class WithdrawalService {

    private final PlatformLedger platformLedger;
    private final CampaignRepository campaignRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository; // <-- ADD DEPENDENCY

    @Autowired
    public WithdrawalService(PlatformLedger platformLedger, CampaignRepository campaignRepository,
                             WithdrawalRequestRepository withdrawalRequestRepository, FileStorageService fileStorageService,
                             UserRepository userRepository) { // <-- ADD TO CONSTRUCTOR
        this.platformLedger = platformLedger;
        this.campaignRepository = campaignRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository; // <-- INITIALIZE IT
    }

    @Transactional
    public WithdrawalRequest createRff(CreateWithdrawalRequest request, MultipartFile financialProof,
                                       MultipartFile visualProof, User charityAdminPrincipal) {

        // --- THE FIX ---
        // Re-fetch the user from the database to get a session-managed entity
        User managedCharityAdmin = userRepository.findById(charityAdminPrincipal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));
        // --- END OF FIX ---

        Charity charity = managedCharityAdmin.getCharity();
        if (charity == null || !charity.getAdminUser().getId().equals(managedCharityAdmin.getId())) {
            throw new IllegalStateException("User is not the designated admin for a charity.");
        }

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found."));

        String financialProofUrl = fileStorageService.storeFile(financialProof);
        String visualProofUrl = fileStorageService.storeFile(visualProof);

        try {
            // Note: Converting BigDecimal to BigInteger loses decimal info.
            // For crypto, we work in the smallest unit (Wei), so this is okay.
            // Make sure the frontend sends the amount in the base currency (e.g., Ether, not Gwei).
            BigInteger amountInWei = request.getAmount().multiply(new BigDecimal("1000000000000000000")).toBigInteger();

            platformLedger.createWithdrawalRequest(
                    BigInteger.valueOf(campaign.getId()),
                    request.getVendorAddress(),
                    amountInWei, // Send amount in Wei
                    request.getPurpose(),
                    financialProofUrl,
                    visualProofUrl
            ).send();

            BigInteger onChainId = platformLedger.nextRequestId().send().subtract(BigInteger.ONE);

            WithdrawalRequest rff = new WithdrawalRequest();
            rff.setOnChainRequestId(onChainId.longValue());
            rff.setCampaign(campaign);
            rff.setAmount(request.getAmount());
            rff.setPurpose(request.getPurpose());
            rff.setVendorAddress(request.getVendorAddress());
            rff.setFinancialProofUrl(financialProofUrl);
            rff.setVisualProofUrl(visualProofUrl);
            // Status and deadline are set via @PrePersist and smart contract

            return withdrawalRequestRepository.save(rff);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create on-chain withdrawal request: " + e.getMessage(), e);
        }
    }
}