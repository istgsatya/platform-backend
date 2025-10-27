package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')") // Controller-level security
public class AdminController {

    private final WithdrawalService withdrawalService;

    @Autowired
    public AdminController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<?> adminApproveRequest(@PathVariable("id") Long withdrawalRequestId) {
        try {
            withdrawalService.adminApproveRequest(withdrawalRequestId);
            return ResponseEntity.ok(new MessageResponse(true, "Admin approval successful. Request has been executed."));
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse(false, "Failed to approve request: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<?> adminRejectRequest(@PathVariable("id") Long withdrawalRequestId) {
        try {
            withdrawalService.adminRejectRequest(withdrawalRequestId);
            return ResponseEntity.ok(new MessageResponse(true, "Admin rejection successful. Request has been rejected and the charity is on a 24-hour cooldown."));
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse(false, "Failed to reject request: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}