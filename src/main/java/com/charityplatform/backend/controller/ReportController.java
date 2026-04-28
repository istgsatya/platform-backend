package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.CreateReportRequest;
import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<?> createReport(
            @Valid @RequestBody CreateReportRequest createReportRequest,
            @AuthenticationPrincipal User currentUser) {
        try {
            reportService.createReport(createReportRequest, currentUser);
            return new ResponseEntity<>(new MessageResponse(true, "Report submitted successfully. It will be reviewed by an admin."), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new MessageResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}