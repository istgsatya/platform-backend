package com.charityplatform.backend.service;

import com.charityplatform.backend.contracts.PlatformLedger;
import com.charityplatform.backend.dto.CreateWithdrawalRequest;
import com.charityplatform.backend.dto.WithdrawalResponseDTO;
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple12;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final CharityRepository charityRepository;
    private final DonationRepository donationRepository;
    private final RestTemplate restTemplate;

    private final IpfsService ipfsService;



    @Autowired
    public WithdrawalService(PlatformLedger platformLedger, CampaignRepository campaignRepository,
                             WithdrawalRequestRepository withdrawalRequestRepository, FileStorageService fileStorageService,
                             UserRepository userRepository, VoteRepository voteRepository, CharityRepository charityRepository,
                             DonationRepository donationRepository, RestTemplate restTemplate,IpfsService ipfsService) {
        this.platformLedger = platformLedger;
        this.campaignRepository = campaignRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.charityRepository = charityRepository;
        this.donationRepository = donationRepository;
        this.restTemplate = restTemplate;
        this.ipfsService = ipfsService;
    }

    @Transactional
    public void verifyAndRecordVote(Long withdrawalRequestId, String txHash, User currentUser) {
        User managedCurrentUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in the database."));

        if (voteRepository.existsByTransactionHash(txHash)) {
            throw new IllegalStateException("This transaction hash has already been used to record a vote.");
        }
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("WithdrawalRequest not found."));
        if (voteRepository.existsByVoterIdAndWithdrawalRequestId(managedCurrentUser.getId(), withdrawalRequestId)) {
            throw new IllegalStateException("You have already recorded a vote for this request.");
        }

        String url = String.format(
                "https://api.etherscan.io/v2/api?chainid=11155111&module=proxy&action=eth_getTransactionReceipt&txhash=%s&apikey=6HH4RNGIHXMJ3G51V7HFKWSB9TQ9QA5DR6",
                txHash
        );

        JsonNode root;
        try {
            root = restTemplate.getForObject(url, JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call the Etherscan API.", e);
        }

        if (root == null || !root.has("result") || root.get("result").isNull()) {
            throw new RuntimeException("Transaction receipt not found on Etherscan for hash: " + txHash);
        }
        JsonNode result = root.get("result");

        if (!result.has("status") || !result.get("status").asText().equals("0x1")) {
            throw new RuntimeException("The transaction was found, but it FAILED on-chain. Hash: " + txHash);
        }
        String toAddress = result.get("to").asText();
        if (!toAddress.equalsIgnoreCase(platformLedger.getContractAddress())) {
            throw new AccessDeniedException("This transaction was not sent to the PlatformLedger smart contract.");
        }

        String fromAddress = result.get("from").asText();
        String userWallet = managedCurrentUser.getWallets().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Authenticated user does not have a registered wallet."))
                .getAddress();

        if (!fromAddress.equalsIgnoreCase(userWallet)) {
            throw new AccessDeniedException("Proof of vote is invalid. The transaction was not sent from your registered wallet.");
        }

        log.info("VERIFICATION SUCCESSFUL for tx [{}]. Saving vote to database.", txHash);
        Vote vote = new Vote();
        vote.setVoter(managedCurrentUser);
        vote.setWithdrawalRequest(request);
        vote.setApproved(true);
        vote.setTransactionHash(txHash);
        voteRepository.save(vote);

        try {
            checkAndTriggerEarlyExecution(request);
        } catch (Exception e) {
            log.error("Vote was successfully saved, but the post-verification execution check failed.", e);
        }
    }

    private void checkAndTriggerEarlyExecution(WithdrawalRequest request) throws Exception {
        Tuple12<BigInteger, String, String, BigInteger, String, String, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> onChainRequest =
                platformLedger.withdrawalRequests(BigInteger.valueOf(request.getOnChainRequestId())).send();
        BigInteger onChainStatus = onChainRequest.component10();
        if (onChainStatus.intValue() != 0) { return; }

        BigInteger votesForInWei = onChainRequest.component11();
        Long campaignId = request.getCampaign().getId();
        BigDecimal totalDonationPowerInETH = donationRepository.getTotalDonationAmountForCampaign(campaignId);
        if (totalDonationPowerInETH == null || totalDonationPowerInETH.compareTo(BigDecimal.ZERO) == 0) { return; }

        BigDecimal weiPerEth = new BigDecimal("1000000000000000000");
        BigDecimal totalDonationPowerInWei = totalDonationPowerInETH.multiply(weiPerEth);

        if (totalDonationPowerInWei.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal actualApprovalPercentage = new BigDecimal(votesForInWei).multiply(new BigDecimal(100))
                    .divide(totalDonationPowerInWei, 2, RoundingMode.HALF_UP);
            log.info("VOTE TALLY for Request {}: {}% FOR.", request.getOnChainRequestId(), actualApprovalPercentage);

            if (actualApprovalPercentage.compareTo(new BigDecimal("60")) > 0) {
                log.warn("THRESHOLD MET! Executing withdrawal for request {}.", request.getOnChainRequestId());
                TransactionReceipt executionReceipt = platformLedger.forceExecuteByAdmin(BigInteger.valueOf(request.getOnChainRequestId())).send();
                if (executionReceipt.isStatusOK()) {
                    request.setStatus(RequestStatus.EXECUTED);
                    withdrawalRequestRepository.save(request);
                    log.info("SUCCESS: Execution transaction confirmed for request {}", request.getOnChainRequestId());
                } else {
                    log.error("Execution FAILED for request {}. On-chain status: {}", request.getOnChainRequestId(), executionReceipt.getStatus());
                }
            }
        }
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
            throw new AccessDeniedException("This charity is on a 24-hour cooldown.");
        }
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + request.getCampaignId()));
        if (!campaign.getCharity().getId().equals(charity.getId())) {
            throw new AccessDeniedException("You do not have permission to create this request.");
        }

        String financialProofCid = null;
        String visualProofCid = null;

        try {
            log.info("IPFS_INTEGRATION: Uploading financial proof to IPFS via Pinata...");
            financialProofCid = ipfsService.uploadFile(financialProof);

            log.info("IPFS_INTEGRATION: Uploading visual proof to IPFS via Pinata...");
            visualProofCid = ipfsService.uploadFile(visualProof);

            BigInteger amountInWei = request.getAmount().multiply(new BigDecimal("1000000000000000000")).toBigInteger();

            log.info("IPFS_INTEGRATION: Sending on-chain transaction with IPFS CIDs: [Financial: {}, Visual: {}]", financialProofCid, visualProofCid);
            TransactionReceipt receipt = platformLedger.createWithdrawalRequest(
                    BigInteger.valueOf(campaign.getId()),
                    request.getVendorAddress(),
                    amountInWei,
                    request.getPurpose(),
                    financialProofCid, // Using the immutable IPFS hash
                    visualProofCid    // Using the immutable IPFS hash
            ).send();

            if (!receipt.isStatusOK()) {
                throw new RuntimeException("Blockchain transaction failed with status: " + receipt.getStatus());
            }
            List<PlatformLedger.WithdrawalRequestCreatedEventResponse> events = platformLedger.getWithdrawalRequestCreatedEvents(receipt);
            if (events.isEmpty()) {
                throw new RuntimeException("Transaction succeeded but failed to emit the expected on-chain event.");
            }

            BigInteger onChainId = events.get(0).requestId;
            BigInteger onChainDeadline = events.get(0).votingDeadline;

            WithdrawalRequest rff = new WithdrawalRequest();
            rff.setOnChainRequestId(onChainId.longValue());
            rff.setCampaign(campaign);
            rff.setAmount(request.getAmount());
            rff.setPurpose(request.getPurpose());
            rff.setVendorAddress(request.getVendorAddress());
            rff.setFinancialProofUrl(financialProofCid); // Saving the IPFS hash to the database
            rff.setVisualProofUrl(visualProofCid);     // Saving the IPFS hash to the database
            rff.setVotingDeadline(Instant.ofEpochSecond(onChainDeadline.longValue()));

            log.info("IPFS_INTEGRATION: Successfully created on-chain request {} and saving to database.", onChainId);
            return withdrawalRequestRepository.save(rff);

        } catch (Exception e) {
            log.error("CRITICAL: Failed during RFF creation with IPFS. Error: {}", e.getMessage(), e);

            // Note: In a production system, you would add logic here to call a "delete" or "unpin" endpoint on Pinata
            // for financialProofCid and visualProofCid if they are not null, to avoid orphaned files.
            // For the hackathon, this manual cleanup step is acceptable.

            throw new RuntimeException("Failed to create the withdrawal request with IPFS proofs: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<WithdrawalResponseDTO> getWithdrawalsForCampaign(Long campaignId) {
        return withdrawalRequestRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId).stream()
                .map(WithdrawalResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getVoteCountForRequest(Long withdrawalRequestId) {
        if (!withdrawalRequestRepository.existsById(withdrawalRequestId)) { return 0; }
        return voteRepository.countDistinctVotersByRequestId(withdrawalRequestId);
    }

    @Transactional(readOnly = true)
    public boolean checkIfUserHasVoted(Long withdrawalRequestId, User currentUser) throws Exception {
        if (currentUser == null) { return false; }
        User managedUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (managedUser == null || managedUser.getWallets().isEmpty()) { return false; }

        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return platformLedger.hasVoted(BigInteger.valueOf(request.getOnChainRequestId()), managedUser.getWallets().get(0).getAddress()).send();
    }

    @Transactional
    public void manuallyTriggerExecution(Long withdrawalRequestId) throws Exception {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("Request not found."));
        checkAndTriggerEarlyExecution(request);
    }

    @Transactional
    public void appealForReview(Long withdrawalRequestId, User currentUser) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalRequestId)
                .orElseThrow(() -> new RuntimeException("WithdrawalRequest not found with id: " + withdrawalRequestId));
        Long charityAdminId = request.getCampaign().getCharity().getAdminUser().getId();
        if (!charityAdminId.equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the admin for the charity that created this request.");
        }
        if (request.getStatus() != RequestStatus.PENDING_VOTE) {
            throw new IllegalStateException("This request is not in a PENDING_VOTE state and cannot be appealed.");
        }
        Instant appealAvailableTime = request.getCreatedAt().plus(15, ChronoUnit.SECONDS);
        if (Instant.now().isBefore(appealAvailableTime)) {
            throw new IllegalStateException("You cannot appeal this request yet. Please wait until " + appealAvailableTime);
        }
        request.setStatus(RequestStatus.AWAITING_ADMIN_DECISION);
        withdrawalRequestRepository.save(request);
        log.info("Request {} successfully appealed by {}. Status updated to AWAITING_ADMIN_DECISION.", withdrawalRequestId, currentUser.getUsername());
    }

    @Transactional
    public void adminApproveRequest(Long withdrawalRequestId) throws Exception {
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
        log.info("SUCCESS: Request {} has been force-rejected. Charity {} is on cooldown.", withdrawalRequestId, charity.getName());
    }
}