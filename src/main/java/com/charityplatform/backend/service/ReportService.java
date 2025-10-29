package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.CreateReportRequest;
import com.charityplatform.backend.model.Report;
import com.charityplatform.backend.model.ReportStatus;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.WithdrawalRequest;
import com.charityplatform.backend.repository.ReportRepository;
import com.charityplatform.backend.repository.UserRepository;
import com.charityplatform.backend.repository.WithdrawalRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    // Define how many points a valid report is worth
    private static final int TRUST_POINTS_PER_VALID_REPORT = 10;

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository, UserRepository userRepository, WithdrawalRequestRepository withdrawalRequestRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
    }

    @Transactional
    public Report createReport(CreateReportRequest createReportRequest, User currentUser) {
        // Fetch the managed user entity to ensure it's attached to the session
        User reporter = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        // Fetch the withdrawal request that is being reported
        WithdrawalRequest reportedRequest = withdrawalRequestRepository.findById(createReportRequest.getReportedRequestId())
                .orElseThrow(() -> new RuntimeException("Withdrawal request to be reported not found."));

        // Create the new report entity
        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedRequest(reportedRequest);
        report.setReason(createReportRequest.getReason());
        // The @PrePersist annotation on the Report entity will automatically set the status to PENDING

        // Save and return the new report
        return reportRepository.save(report);
    }

    // --- START: NEW GAMIFICATION LOGIC METHOD ---
    @Transactional
    public String validateReport(Long reportId, boolean isValid) {
        // 1. Fetch the report
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report with ID " + reportId + " not found."));

        // 2. Guard Clause: Check if it's already been reviewed.
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("This report has already been reviewed and its status is " + report.getStatus());
        }

        if (isValid) {
            // 3a. The report is valid. Update status and reward the user.
            report.setStatus(ReportStatus.VALIDATED);
            User reporter = report.getReporter();

            int newTrustPoints = reporter.getTrustPoints() + TRUST_POINTS_PER_VALID_REPORT;
            reporter.setTrustPoints(newTrustPoints);

            // Here you could add logic to upgrade vipLevel, e.g., if (newTrustPoints >= 100) { reporter.setVipLevel(1); }

            userRepository.save(reporter); // This save cascades the trust point update
            reportRepository.save(report);

            return "Report " + reportId + " has been validated. User '" + reporter.getUsername() + "' has been awarded " + TRUST_POINTS_PER_VALID_REPORT + " trust points.";
        } else {
            // 3b. The report is invalid. Just update the status.
            report.setStatus(ReportStatus.REJECTED);
            reportRepository.save(report);

            return "Report " + reportId + " has been rejected.";
        }
    }
    // --- END: NEW GAMIFICATION LOGIC METHOD ---
}