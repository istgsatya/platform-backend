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
@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

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
}
