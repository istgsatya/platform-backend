package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.dto.ValidateReportRequest;
import com.charityplatform.backend.service.ReportService;
import com.charityplatform.backend.service.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')") // Controller-level security
public class AdminController {

    private final WithdrawalService withdrawalService;
    private final ReportService reportService;


    @Autowired
    public AdminController(WithdrawalService withdrawalService, ReportService reportService) {
        this.withdrawalService = withdrawalService;
        this.reportService = reportService;


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
    @PostMapping("/reports/{id}/validate")
    public ResponseEntity<?> valideReport(@PathVariable("id")Long reportId,@Valid @RequestBody ValidateReportRequest validateRequest){
        try {
            // The service will handle all the logic and return a confirmation message.
            String message = reportService.validateReport(reportId, validateRequest.getIsValid());
            return ResponseEntity.ok(new MessageResponse(true, message));
        } catch (RuntimeException e) {
            // For errors like "Report not found" or "Report already validated"
            return new ResponseEntity<>(new MessageResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        } 
    }
}