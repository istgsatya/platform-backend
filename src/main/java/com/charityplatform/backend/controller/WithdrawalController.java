package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.CreateWithdrawalRequest;
import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.WithdrawalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalController.class);

    private final WithdrawalService withdrawalService;

    @Autowired
    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')")
    public ResponseEntity<?> createWithdrawalRequest(
            @RequestPart("request") CreateWithdrawalRequest request,
            @RequestPart("financialProof") MultipartFile financialProof,
            @RequestPart("visualProof") MultipartFile visualProof,
            @AuthenticationPrincipal User currentUser) {
        withdrawalService.createRff(request, financialProof, visualProof, currentUser);
        return ResponseEntity.ok(new MessageResponse(true, "Withdrawal Request created successfully and is now open for voting."));
    }

    // --- START: NEW VOTE ENDPOINTS ---





    // --- END: NEW VOTE ENDPOINTS ---

    @PostMapping("/{id}/appeal")
    @PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')")
    public ResponseEntity<?> appealRequest(
            @PathVariable("id") Long withdrawalRequestId,
            @AuthenticationPrincipal User currentUser) {
        try {
            withdrawalService.appealForReview(withdrawalRequestId, currentUser);
            return ResponseEntity.ok(new MessageResponse(true, "Request successfully appealed and sent for admin review."));
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new MessageResponse(false, e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new MessageResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new MessageResponse(false, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/votecount")
    public ResponseEntity<?> getVoteCount(@PathVariable("id") Long id) {
        long count = withdrawalService.getVoteCountForRequest(id);
        return ResponseEntity.ok(Map.of("voteCount", count));
    }

    @PostMapping("/{id}/manual-trigger")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<?> manuallyTriggerExecutionCheck(
            @PathVariable("id") Long withdrawalRequestId,
            @AuthenticationPrincipal User currentUser) {
        try {
            log.warn("ADMIN {} is manually triggering an execution check for request {}", currentUser.getUsername(), withdrawalRequestId);
            withdrawalService.manuallyTriggerExecution(withdrawalRequestId);
            return ResponseEntity.ok(new MessageResponse(true, "Manual execution check triggered successfully. Check logs for outcome."));
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse(false, "An error occurred: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/has-voted")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<?> hasUserVoted(
            @PathVariable("id") Long withdrawalRequestId,
            @AuthenticationPrincipal User currentUser) {
        try {
            boolean hasVoted = withdrawalService.checkIfUserHasVoted(withdrawalRequestId, currentUser);
            return ResponseEntity.ok(Map.of("hasVoted", hasVoted));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("hasVoted", false));
        }
    }


    // ADD THIS METHOD TO WithdrawalController.java

    @PostMapping("/{id}/verify-vote")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<?> verifyVoteTransaction(
            @PathVariable("id") Long withdrawalRequestId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {

        log.info("CONTROLLER_HIT: /verify-vote received for request [{}]", withdrawalRequestId);
        String txHash = body.get("transactionHash");

        if (txHash == null || txHash.isBlank()) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, "Request body must contain 'transactionHash'."));
        }

        try {
            withdrawalService.verifyAndRecordVote(withdrawalRequestId, txHash, currentUser);
            return ResponseEntity.ok(new MessageResponse(true, "Your vote has been successfully verified and recorded."));
        } catch (Exception e) {
            log.error("VOTE VERIFICATION FAILED: Error for request [{}]. Error: {}", withdrawalRequestId, e.getMessage());
            return new ResponseEntity<>(new MessageResponse(false, "Vote verification failed: " + e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}