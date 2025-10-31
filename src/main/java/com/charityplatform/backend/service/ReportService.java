package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.CreateReportRequest;
import com.charityplatform.backend.dto.ReportResponseDTO; // <-- New Import
import com.charityplatform.backend.model.*;
import com.charityplatform.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // <-- New Import
import java.util.stream.Collectors; // <-- New Import

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final int TRUST_POINTS_PER_VALID_REPORT = 10;
    private static final int BLACKLIST_THRESHOLD = 3;

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final CharityRepository charityRepository;
    private final BlacklistedIdentifierRepository blacklistedIdentifierRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository, UserRepository userRepository,
                         WithdrawalRequestRepository withdrawalRequestRepository, CharityRepository charityRepository,
                         BlacklistedIdentifierRepository blacklistedIdentifierRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.charityRepository = charityRepository;
        this.blacklistedIdentifierRepository = blacklistedIdentifierRepository;
    }

    @Transactional
    public Report createReport(CreateReportRequest createReportRequest, User currentUser) {
        User reporter = userRepository.findById(currentUser.getId()).orElseThrow(() -> new RuntimeException("Authenticated user not found."));
        WithdrawalRequest reportedRequest = withdrawalRequestRepository.findById(createReportRequest.getReportedRequestId()).orElseThrow(() -> new RuntimeException("Withdrawal request to be reported not found."));
        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedRequest(reportedRequest);
        report.setReason(createReportRequest.getReason());
        return reportRepository.save(report);
    }

    @Transactional
    public String validateReport(Long reportId, boolean isValid) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report with ID " + reportId + " not found."));
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("This report has already been reviewed and its status is " + report.getStatus());
        }
        if (isValid) {
            report.setStatus(ReportStatus.VALIDATED);
            User reporter = report.getReporter();
            reporter.setTrustPoints(reporter.getTrustPoints() + TRUST_POINTS_PER_VALID_REPORT);
            userRepository.save(reporter);
            reportRepository.save(report);

            Charity reportedCharity = report.getReportedRequest().getCampaign().getCharity();
            int newReportCount = reportedCharity.getValidatedReportCount() + 1;
            reportedCharity.setValidatedReportCount(newReportCount);

            if (newReportCount >= BLACKLIST_THRESHOLD) {
                blacklistCharity(reportedCharity);
                return "Report " + reportId + " validated. Charity '" + reportedCharity.getName() + "' has been PERMANENTLY BLACKLISTED.";
            } else {
                charityRepository.save(reportedCharity);
                return "Report " + reportId + " validated. Charity '" + reportedCharity.getName() + "' now has " + newReportCount + " validated reports against it.";
            }
        } else {
            report.setStatus(ReportStatus.REJECTED);
            reportRepository.save(report);
            return "Report " + reportId + " has been rejected.";
        }
    }


    @Transactional(readOnly = true)
    public List<ReportResponseDTO> getPendingReports() {

        List<Report> pendingReports = reportRepository.findByStatus(ReportStatus.PENDING);


        return pendingReports.stream()
                .map(ReportResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }



    private void blacklistCharity(Charity charity) {
        log.warn("BLACKLISTING charity '{}' (ID: {}). Three strikes rule violated.", charity.getName(), charity.getId());
        charity.setStatus(VerificationStatus.BLACKLISTED);
        charityRepository.save(charity);

        User admin = charity.getAdminUser();
        if (admin != null && !admin.getWallets().isEmpty()) {
            String adminWallet = admin.getWallets().get(0).getAddress();
            createBlacklistEntry(adminWallet, IdentifierType.ETH_ADDRESS, "Admin wallet of blacklisted charity ID: " + charity.getId());
        }

        String storedFileName = charity.getRegistrationDocumentUrl();
        if (storedFileName != null && storedFileName.contains(".")) {
            String originalFileName = storedFileName.substring(storedFileName.indexOf('.') + 1);
            createBlacklistEntry(originalFileName, IdentifierType.REGISTRATION_DOCUMENT_URL, "Registration document of blacklisted charity ID: " + charity.getId());
        }
    }

    private void createBlacklistEntry(String value, IdentifierType type, String reason) {
        if (value == null || value.isBlank()) return;
        if (!blacklistedIdentifierRepository.existsByIdentifierValueAndIdentifierType(value, type)) {
            BlacklistedIdentifier entry = new BlacklistedIdentifier();
            entry.setIdentifierValue(value);
            entry.setIdentifierType(type);
            entry.setReason(reason);
            blacklistedIdentifierRepository.save(entry);
            log.info("Added '{}' to the blacklist. Reason: {}", value, reason);
        }
    }
}