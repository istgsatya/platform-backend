package com.charityplatform.backend.service;

import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CreateWithdrawalRequest;
import com.charityplatform.backend.model.Campaign;
import com.charityplatform.backend.model.Charity;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.WithdrawalRequest;
import com.charityplatform.backend.repository.CampaignRepository;
import com.charityplatform.backend.repository.UserRepository;
import com.charityplatform.backend.repository.WithdrawalRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.VoteRepository;
@Service
public class WithdrawalService {

    private final PlatformLedger platformLedger;
    private final CampaignRepository campaignRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    @Autowired
    public WithdrawalService(PlatformLedger platformLedger, CampaignRepository campaignRepository,
                             WithdrawalRequestRepository withdrawalRequestRepository, FileStorageService fileStorageService,
                             UserRepository userRepository, VoteRepository voteRepository) {
        this.platformLedger = platformLedger;
        this.campaignRepository = campaignRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public WithdrawalRequest createRff(CreateWithdrawalRequest request, MultipartFile financialProof,
                                       MultipartFile visualProof, User charityAdminPrincipal) {


        User managedCharityAdmin = userRepository.findById(charityAdminPrincipal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));

        Charity charity = managedCharityAdmin.getCharity();
        if (charity == null || !charity.getAdminUser().getId().equals(managedCharityAdmin.getId())) {
            throw new IllegalStateException("User is not the designated admin for a charity.");
        }

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + request.getCampaignId()));


        if (!campaign.getCharity().getId().equals(charity.getId())) {
            throw new AccessDeniedException("You do not have permission to create a withdrawal request for this campaign.");
        }

        String financialProofUrl = fileStorageService.storeFile(financialProof);
        String visualProofUrl = fileStorageService.storeFile(visualProof);

        try {
            BigInteger amountInWei = request.getAmount().multiply(new BigDecimal("1000000000000000000")).toBigInteger();

            // --- THE FIX ---
            // Step 1: Send the transaction and get the receipt. This is our ONE on-chain interaction.
            TransactionReceipt receipt = platformLedger.createWithdrawalRequest(
                    BigInteger.valueOf(campaign.getId()),
                    request.getVendorAddress(),
                    amountInWei,
                    request.getPurpose(),
                    financialProofUrl,
                    visualProofUrl
            ).send();

            if (!receipt.isStatusOK()) {
                throw new RuntimeException("Blockchain transaction failed. Status: " + receipt.getStatus());
            }

            // Step 2: Extract the event from the receipt.
            List<PlatformLedger.WithdrawalRequestCreatedEventResponse> events = platformLedger.getWithdrawalRequestCreatedEvents(receipt);
            if (events.isEmpty()) {
                // This is a critical failure. The contract didn't emit the event we expected.
                // We MUST throw an exception and roll back the database transaction.
                throw new RuntimeException("Transaction succeeded but failed to emit WithdrawalRequestCreated event. Aborting.");
            }

            // Step 3: Get the requestId and deadline directly from the parsed event. This is guaranteed to be correct.
            BigInteger onChainId = events.get(0).requestId;
            BigInteger onChainDeadline = events.get(0).votingDeadline;
            // --- END OF FIX ---


            WithdrawalRequest rff = new WithdrawalRequest();
            rff.setOnChainRequestId(onChainId.longValue());
            rff.setCampaign(campaign);
            rff.setAmount(request.getAmount());
            rff.setPurpose(request.getPurpose());
            rff.setVendorAddress(request.getVendorAddress());
            rff.setFinancialProofUrl(financialProofUrl);
            rff.setVisualProofUrl(visualProofUrl);
            // Set the deadline from the on-chain event for perfect sync
            rff.setVotingDeadline(Instant.ofEpochSecond(onChainDeadline.longValue()));
            // Status is set to PENDING_VOTE by the @PrePersist annotation.

            return withdrawalRequestRepository.save(rff);

        } catch (Exception e) {
            // Clean up stored files if the blockchain transaction fails or any other exception occurs.
            fileStorageService.deleteFile(financialProofUrl);
            fileStorageService.deleteFile(visualProofUrl);
            throw new RuntimeException("Failed to create on-chain withdrawal request: " + e.getMessage(), e);
        }
    }
    @Transactional
    public void voteOnRequest(Long withdrawalRequestId, boolean approve, User currentUser) throws Exception {
        // 1. Fetch Managed Entities
        User voter = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));

        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("WithdrawalRequest not found with id: " + withdrawalRequestId));

        if (voter.getWallets().isEmpty()) {
            throw new IllegalStateException("User does not have a wallet registered.");
        }
        String voterAddress = voter.getWallets().get(0).getAddress();
        Long onChainRequestId = request.getOnChainRequestId();
        Long campaignId = request.getCampaign().getId();

        // --- 2. Validation and Guard Clauses (THE FORTRESS) ---

        // Check 1: Is voting still open based on our database's records?
        if (request.getStatus() != RequestStatus.PENDING_VOTE) {
            throw new IllegalStateException("Voting for this request is not currently active.");
        }
        if (Instant.now().isAfter(request.getVotingDeadline())) {
            throw new IllegalStateException("The voting period for this request has ended.");
        }

        // Check 2 (On-Chain): Is this user even eligible to vote? (Did they donate?)
        BigInteger votingPower = platformLedger.campaignContributions(BigInteger.valueOf(campaignId), voterAddress).send();
        if (votingPower.equals(BigInteger.ZERO)) {
            throw new AccessDeniedException("You are not a donor to this campaign and therefore cannot vote.");
        }

        // Check 3 (On-Chain): Have they already voted?
        boolean alreadyVoted = platformLedger.hasVoted(BigInteger.valueOf(onChainRequestId), voterAddress).send();
        if (alreadyVoted) {
            throw new IllegalStateException("You have already voted on this request.");
        }

        // --- 3. The Action: Send the On-Chain Transaction ---
        TransactionReceipt receipt = platformLedger.voteOnRequest(BigInteger.valueOf(onChainRequestId), approve).send();
        if (!receipt.isStatusOK()) {
            throw new RuntimeException("Blockchain transaction to cast vote failed. Status: " + receipt.getStatus());
        }

        // --- 4. The Aftermath: Record the vote in our database for performance ---
        Vote vote = new Vote();
        vote.setVoter(voter);
        vote.setWithdrawalRequest(request);
        vote.setApproved(approve);
        voteRepository.save(vote);
    }
}