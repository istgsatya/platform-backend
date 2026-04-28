package com.charityplatform.backend.dto;

import com.charityplatform.backend.model.Report;
import com.charityplatform.backend.model.ReportStatus;

import java.time.Instant;

public class ReportResponseDTO {

    private Long reportId;
    private String reason;
    private ReportStatus status;
    private Instant createdAt;
    private String reporterUsername;
    private Long reportedRequestId;
    private String reportedRequestPurpose;
    private String reportedCharityName;

    public static ReportResponseDTO fromEntity(Report report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setReportId(report.getId());
        dto.setReason(report.getReason());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());

        if (report.getReporter() != null) {
            dto.setReporterUsername(report.getReporter().getUsername());
        }
        if (report.getReportedRequest() != null) {
            dto.setReportedRequestId(report.getReportedRequest().getId());
            dto.setReportedRequestPurpose(report.getReportedRequest().getPurpose());
            if (report.getReportedRequest().getCampaign() != null &&
                    report.getReportedRequest().getCampaign().getCharity() != null) {
                dto.setReportedCharityName(report.getReportedRequest().getCampaign().getCharity().getName());
            }
        }
        return dto;
    }


    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }
    public Long getReportedRequestId() { return reportedRequestId; }
    public void setReportedRequestId(Long reportedRequestId) { this.reportedRequestId = reportedRequestId; }
    public String getReportedRequestPurpose() { return reportedRequestPurpose; }
    public void setReportedRequestPurpose(String reportedRequestPurpose) { this.reportedRequestPurpose = reportedRequestPurpose; }
    public String getReportedCharityName() { return reportedCharityName; }
    public void setReportedCharityName(String reportedCharityName) { this.reportedCharityName = reportedCharityName; }
}