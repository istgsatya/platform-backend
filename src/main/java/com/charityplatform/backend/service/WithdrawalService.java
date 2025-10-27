package com.charityplatform.backend.service;

import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CreateWithdrawalRequest;
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.CampaignRepository;
import com.charityplatform.backend.repository.UserRepository;
import com.charityplatform.backend.repository.VoteRepository;
import com.charityplatform.backend.repository.WithdrawalRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple12;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

@Service
public class WithdrawalService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalService.class);

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

        // ... This method remains unchanged ...
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
            TransactionReceipt receipt = platformLedger.createWithdrawalRequest(BigInteger.valueOf(campaign.getId()), request.getVendorAddress(), amountInWei, request.getPurpose(), financialProofUrl, visualProofUrl).send();
            if (!receipt.isStatusOK()) {
                throw new RuntimeException("Blockchain transaction failed. Status: " + receipt.getStatus());
            }
            List<PlatformLedger.WithdrawalRequestCreatedEventResponse> events = platformLedger.getWithdrawalRequestCreatedEvents(receipt);
            if (events.isEmpty()) {
                throw new RuntimeException("Transaction succeeded but failed to emit WithdrawalRequestCreated event. Aborting.");
            }
            BigInteger onChainId = events.get(0).requestId;
            BigInteger onChainDeadline = events.get(0).votingDeadline;
            WithdrawalRequest rff = new WithdrawalRequest();
            rff.setOnChainRequestId(onChainId.longValue());
            rff.setCampaign(campaign);
            rff.setAmount(request.getAmount());
            rff.setPurpose(request.getPurpose());
            rff.setVendorAddress(request.getVendorAddress());
            rff.setFinancialProofUrl(financialProofUrl);
            rff.setVisualProofUrl(visualProofUrl);
            rff.setVotingDeadline(Instant.ofEpochSecond(onChainDeadline.longValue()));
            return withdrawalRequestRepository.save(rff);
        } catch (Exception e) {
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

        // --- 2. Validation and Guard Clauses (Unchanged) ---
        if (request.getStatus() != RequestStatus.PENDING_VOTE) {
            throw new IllegalStateException("Voting for this request is not currently active.");
        }
        if (Instant.now().isAfter(request.getVotingDeadline())) {
            throw new IllegalStateException("The voting period for this request has ended.");
        }
        BigInteger votingPower = platformLedger.campaignContributions(BigInteger.valueOf(campaignId), voterAddress).send();
        if (votingPower.equals(BigInteger.ZERO)) {
            throw new AccessDeniedException("You are not a donor to this campaign and therefore cannot vote.");
        }
        boolean alreadyVoted = platformLedger.hasVoted(BigInteger.valueOf(onChainRequestId), voterAddress).send();
        if (alreadyVoted) {
            throw new IllegalStateException("You have already voted on this request.");
        }

        // --- 3. The Action: Send the On-Chain Transaction ---
        TransactionReceipt receipt = platformLedger.voteOnRequest(BigInteger.valueOf(onChainRequestId), approve).send();
        if (!receipt.isStatusOK()) {
            throw new RuntimeException("Blockchain transaction to cast vote failed. Status: " + receipt.getStatus());
        }

        // --- 4. The Off-Chain Record ---
        Vote vote = new Vote();
        vote.setVoter(voter);
        vote.setWithdrawalRequest(request);
        vote.setApproved(approve);
        voteRepository.save(vote);

        // --- 5. THE NEW LOGIC: CHECK AND TRIGGER EARLY EXECUTION ---
        log.info("Vote cast successfully. Now checking if execution threshold is met for request {}", onChainRequestId);
        checkAndTriggerEarlyExecution(request);
    }

    /**
     * This private helper method contains the "hybrid governance" logic.
     * It's called after a vote to check if the request can be executed immediately.
     */
    private void checkAndTriggerEarlyExecution(WithdrawalRequest request) {
        try {
            // Get the latest state of the request directly from the smart contract
            Tuple12<BigInteger, String, String, BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> onChainRequest =
                    platformLedger.withdrawalRequests(BigInteger.valueOf(request.getOnChainRequestId())).send();

            // Based on our V3 contract, votesFor is at index 10 (.getValue11)
            // and votesAgainst is at index 11 (.getValue12).
            BigInteger votesFor = onChainRequest.getValue11();
            BigInteger votesAgainst = onChainRequest.getValue12();
            BigInteger totalVotes = votesFor.add(votesAgainst);

            if (totalVotes.equals(BigInteger.ZERO)) {
                log.info("No votes tallied yet, cannot execute.");
                return; // Nothing to do if no votes are registered
            }

            BigInteger approvalPercentage = votesFor.multiply(BigInteger.valueOf(100)).divide(totalVotes);

            log.info("Request {} vote tally: {}% FOR ({}) / {}% AGAINST ({}).",
                    request.getOnChainRequestId(), approvalPercentage, votesFor, BigInteger.valueOf(100).subtract(approvalPercentage), votesAgainst);

            // If approval is over 60%, trigger the early execution
            if (approvalPercentage.intValue() > 60) {
                log.warn("VOTE THRESHOLD MET ({}%)! Attempting to trigger early execution for request {}.",
                        approvalPercentage, request.getOnChainRequestId());

                TransactionReceipt executionReceipt = platformLedger.triggerEarlyExecution(BigInteger.valueOf(request.getOnChainRequestId())).send();

                if (executionReceipt.isStatusOK()) {
                    log.info("SUCCESS: Early execution transaction confirmed for request {}", request.getOnChainRequestId());
                    // IMPORTANT: Sync our local database to reflect the on-chain state
                    request.setStatus(RequestStatus.EXECUTED);
                    withdrawalRequestRepository.save(request);
                } else {
                    log.error("Execution transaction failed for request {}. Status: {}", request.getOnChainRequestId(), executionReceipt.getStatus());
                }
            }
        } catch (Exception e) {
            // This catch is CRITICAL. We don't want an issue here to fail the user's vote call.
            // This could happen if gas is too low, or if two users vote simultaneously and the other user's
            // vote triggers the execution first. It's a non-fatal error for this user's transaction.
            log.error("An error occurred during the check/trigger early execution phase for request {}. This does not affect the vote itself. Error: {}",
                    request.getOnChainRequestId(), e.getMessage());
        }
    }
}