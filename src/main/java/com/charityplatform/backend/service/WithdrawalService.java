package com.charityplatform.backend.service;

import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CreateWithdrawalRequest;
import com.charityplatform.backend.dto.WithdrawalResponseDTO;
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.*; // Changed to wildcard import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.charityplatform.backend.repository.DonationRepository;
import java.math.RoundingMode;


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
import java.time.temporal.ChronoUnit; // <-- New Import
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WithdrawalService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalService.class);

    private final PlatformLedger platformLedger;
    private final CampaignRepository campaignRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final CharityRepository charityRepository; // <-- New Dependency
    private final DonationRepository donationRepository; // <-- Add this field


    @Autowired
    public WithdrawalService(PlatformLedger platformLedger, CampaignRepository campaignRepository,
                             WithdrawalRequestRepository withdrawalRequestRepository, FileStorageService fileStorageService,
                             UserRepository userRepository, VoteRepository voteRepository, CharityRepository charityRepository,DonationRepository donationRepository) {
        this.platformLedger = platformLedger;
        this.campaignRepository = campaignRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.charityRepository = charityRepository;

        this.donationRepository = donationRepository;
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


        if (charity.getRffCooldownUntil() != null && Instant.now().isBefore(charity.getRffCooldownUntil())) {
            log.warn("Charity '{}' (ID: {}) tried to create an RFF while on cooldown.", charity.getName(), charity.getId());
            throw new AccessDeniedException("This charity is on a 24-hour cooldown from creating new funding requests due to a recently rejected appeal. Cooldown ends at: " + charity.getRffCooldownUntil());
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

        User voter = userRepository.findById(currentUser.getId()).orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId).orElseThrow(() -> new RuntimeException("WithdrawalRequest not found with id: " + withdrawalRequestId));
        if (voter.getWallets().isEmpty()) { throw new IllegalStateException("User does not have a wallet registered."); }
        String voterAddress = voter.getWallets().get(0).getAddress();
        Long onChainRequestId = request.getOnChainRequestId();
        Long campaignId = request.getCampaign().getId();
        if (request.getStatus() != RequestStatus.PENDING_VOTE) { throw new IllegalStateException("Voting for this request is not currently active."); }
        if (Instant.now().isAfter(request.getVotingDeadline())) { throw new IllegalStateException("The voting period for this request has ended."); }
        BigInteger votingPower = platformLedger.campaignContributions(BigInteger.valueOf(campaignId), voterAddress).send();
        if (votingPower.equals(BigInteger.ZERO)) { throw new AccessDeniedException("You are not a donor to this campaign and therefore cannot vote."); }
        boolean alreadyVoted = platformLedger.hasVoted(BigInteger.valueOf(onChainRequestId), voterAddress).send();
        if (alreadyVoted) { throw new IllegalStateException("You have already voted on this request."); }
        TransactionReceipt receipt = platformLedger.voteOnRequest(BigInteger.valueOf(onChainRequestId), approve).send();
        if (!receipt.isStatusOK()) { throw new RuntimeException("Blockchain transaction to cast vote failed. Status: " + receipt.getStatus()); }
        Vote vote = new Vote();
        vote.setVoter(voter);
        vote.setWithdrawalRequest(request);
        vote.setApproved(approve);
        voteRepository.save(vote);
        log.info("Vote cast successfully. Now checking if execution threshold is met for request {}", onChainRequestId);
        checkAndTriggerEarlyExecution(request);
    }
    private void checkAndTriggerEarlyExecution(WithdrawalRequest request) {
        try {
            Tuple12<BigInteger, String, String, BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> onChainRequest =
                    platformLedger.withdrawalRequests(BigInteger.valueOf(request.getOnChainRequestId())).send();

            BigInteger onChainStatus = onChainRequest.component10();
            if (onChainStatus.intValue() != 0) {
                log.info("Request {} is no longer pending on-chain. Skipping execution check.", request.getOnChainRequestId());
                return;
            }

            BigInteger votesForInWei = onChainRequest.component11();
            Long campaignId = request.getCampaign().getId();
            BigDecimal totalDonationPowerInETH = donationRepository.getTotalDonationAmountForCampaign(campaignId);

            if (totalDonationPowerInETH == null || totalDonationPowerInETH.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Campaign {} has no donation power recorded in the database. Cannot calculate threshold.", campaignId);
                return;
            }

            BigDecimal weiPerEth = new BigDecimal("1000000000000000000");
            BigDecimal totalDonationPowerInWei = totalDonationPowerInETH.multiply(weiPerEth);

            BigDecimal votesForDecimal = new BigDecimal(votesForInWei);
            BigDecimal sixtyPercentThreshold = new BigDecimal("60");
            BigDecimal actualApprovalPercentage = votesForDecimal.multiply(new BigDecimal(100))
                    .divide(totalDonationPowerInWei, 2, RoundingMode.HALF_UP);

            log.info("CORRECT VOTE TALLY for Request {}: {}% FOR ({}) / {} total power.",
                    request.getOnChainRequestId(), actualApprovalPercentage, votesForInWei, totalDonationPowerInWei);

            if (actualApprovalPercentage.compareTo(sixtyPercentThreshold) > 0) {
                log.warn("CORRECT VOTE THRESHOLD MET ({}%)! Attempting to execute via admin function for request {}.",
                        actualApprovalPercentage, request.getOnChainRequestId());

                TransactionReceipt executionReceipt = platformLedger.forceExecuteByAdmin(BigInteger.valueOf(request.getOnChainRequestId())).send();

                if (executionReceipt.isStatusOK()) {
                    log.info("SUCCESS: Admin-forced execution transaction confirmed for request {}", request.getOnChainRequestId());
                    request.setStatus(RequestStatus.EXECUTED);
                    withdrawalRequestRepository.save(request);
                } else {
                    log.error("Execution transaction FAILED for request {}. On-chain status: {}",
                            request.getOnChainRequestId(), executionReceipt.getStatus());
                }
            }
        } catch (Exception e) {
            log.error("CRITICAL: An error occurred during the 'Backend Adjudicator' check for request {}. The user's vote was saved, but execution failed. Error: {}",
                    request.getOnChainRequestId(), e.getMessage());
        }
    }


    @Transactional
    public void appealForReview(Long withdrawalRequestId, User currentUser) {
        log.info("User {} is attempting to appeal request ID {}", currentUser.getUsername(), withdrawalRequestId);
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("WithdrawalRequest not found with id: " + withdrawalRequestId));


        Long charityAdminId = request.getCampaign().getCharity().getAdminUser().getId();
        if (!charityAdminId.equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the admin for the charity that created this request.");
        }

        // State check: can only appeal requests that are still being voted on
        if (request.getStatus() != RequestStatus.PENDING_VOTE) {
            throw new IllegalStateException("This request is not in a PENDING_VOTE state and cannot be appealed.");
        }

        // Timelock check: must wait 12 hours
        // HACKATHON TEST MODE: Timelock set to 15 seconds
        Instant appealAvailableTime = request.getCreatedAt().plus(15, ChronoUnit.SECONDS);
      //////////////////////  Instant appealAvailableTime = request.getCreatedAt().plus(12, ChronoUnit.HOURS);
        if (Instant.now().isBefore(appealAvailableTime)) {
            throw new IllegalStateException("You cannot appeal this request yet. Please wait until " + appealAvailableTime);
        }


        request.setStatus(RequestStatus.AWAITING_ADMIN_DECISION);
        withdrawalRequestRepository.save(request);
        log.info("Request {} successfully appealed by {}. Status updated to AWAITING_ADMIN_DECISION.", withdrawalRequestId, currentUser.getUsername());
    }

    @Transactional
    public void adminApproveRequest(Long withdrawalRequestId) throws Exception {
        log.warn("PLATFORM ADMIN attempting to FORCE APPROVE request ID {}", withdrawalRequestId);
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("WithdrawalRequest not found with id: " + withdrawalRequestId));

        if (request.getStatus() != RequestStatus.AWAITING_ADMIN_DECISION) {
            throw new IllegalStateException("This request is not awaiting admin decision and cannot be force-approved.");
        }

        TransactionReceipt receipt = platformLedger.forceExecuteByAdmin(BigInteger.valueOf(request.getOnChainRequestId())).send();
        if (!receipt.isStatusOK()) {
            throw new RuntimeException("Blockchain transaction to force-execute failed. Status: " + receipt.getStatus());
        }

        request.setStatus(RequestStatus.EXECUTED);
        withdrawalRequestRepository.save(request);
        log.info("SUCCESS: Request {} has been force-approved by Platform Admin.", withdrawalRequestId);
    }

    @Transactional
    public void adminRejectRequest(Long withdrawalRequestId) throws Exception {
        log.warn("PLATFORM ADMIN attempting to FORCE REJECT request ID {}", withdrawalRequestId);
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("WithdrawalRequest not found with id: " + withdrawalRequestId));

        if (request.getStatus() != RequestStatus.AWAITING_ADMIN_DECISION) {
            throw new IllegalStateException("This request is not awaiting admin decision and cannot be force-rejected.");
        }


        TransactionReceipt receipt = platformLedger.forceRejectByAdmin(BigInteger.valueOf(request.getOnChainRequestId())).send();
        if (!receipt.isStatusOK()) {
            throw new RuntimeException("Blockchain transaction to force-reject failed. Status: " + receipt.getStatus());
        }

        request.setStatus(RequestStatus.REJECTED);
        withdrawalRequestRepository.save(request);


        Charity charity = request.getCampaign().getCharity();
        Instant cooldownEnd = Instant.now().plus(24, ChronoUnit.HOURS);
        charity.setRffCooldownUntil(cooldownEnd);
        charityRepository.save(charity);

        log.info("SUCCESS: Request {} has been force-rejected by Platform Admin. Charity {} is on cooldown until {}",
                withdrawalRequestId, charity.getName(), cooldownEnd);
    }
    @Transactional(readOnly = true)
    public List<WithdrawalResponseDTO> getWithdrawalsForCampaign(Long campaignId) {

        List<WithdrawalRequest> requests = withdrawalRequestRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId);


        return requests.stream()
                .map(WithdrawalResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public long getVoteCountForRequest(Long withdrawalRequestId) {

        if (!withdrawalRequestRepository.existsById(withdrawalRequestId)) {
            throw new RuntimeException("WithdrawalRequest not found with id: " + withdrawalRequestId);
        }
        return voteRepository.countDistinctVotersByRequestId(withdrawalRequestId);
    }



    // This new method allows an admin to kickstart the check for a stuck request.
    @Transactional
    public void manuallyTriggerExecution(Long withdrawalRequestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("WithdrawalRequest not found with id: " + withdrawalRequestId));

        log.info("Found request {}. Calling checkAndTriggerEarlyExecution logic.", withdrawalRequestId);
        // This calls the exact same, newly-fixed method.
        checkAndTriggerEarlyExecution(request);
    }
    @Transactional(readOnly = true)
    public boolean checkIfUserHasVoted(Long withdrawalRequestId, User currentUser) throws Exception {
        if (currentUser == null || currentUser.getWallets().isEmpty()) {
            return false; // User not logged in or has no wallet
        }
        String userWallet = currentUser.getWallets().get(0).getAddress();
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // Query the blockchain directly for the most accurate answer
        return platformLedger.hasVoted(BigInteger.valueOf(request.getOnChainRequestId()), userWallet).send();
    }
}