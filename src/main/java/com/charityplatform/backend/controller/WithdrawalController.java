package com.charityplatform.backend.controller;


import com.charityplatform.backend.dto.CreateWithdrawalRequest;
import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.dto.VoteRequest;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import org.springframework.security.access.AccessDeniedException;

import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            @AuthenticationPrincipal User currentUser
    ) {

        withdrawalService.createRff(request, financialProof, visualProof, currentUser);
        return ResponseEntity.ok(new MessageResponse(true, "Withdrawal Request created successfully and is now open for voting."));
    }
    @PostMapping("/{id}/vote")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<?> castVoteOnRequest(
            @PathVariable("id") Long withdrawalRequestId,
            @RequestBody VoteRequest voteRequest,
            @AuthenticationPrincipal User currentUser) {
        try {
            withdrawalService.voteOnRequest(withdrawalRequestId, voteRequest.getApprove(), currentUser);
            return ResponseEntity.ok(new MessageResponse(true, "Your vote has been successfully cast on the blockchain."));
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new MessageResponse(false, e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (IllegalStateException e) {

            return new ResponseEntity<>(new MessageResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            return new ResponseEntity<>(new MessageResponse(false, "An unexpected error occurred: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



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
    public ResponseEntity<?> getVoteCount(@PathVariable Long id) {
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
            // Return false if any error occurs, preventing a locked UI on a failed check
            return ResponseEntity.ok(Map.of("hasVoted", false));
        }
    }



}
