package com.scripto.backend.admin.dto;

import com.scripto.backend.report.domain.ReportReason;
import com.scripto.backend.report.domain.ReportStatus;
import com.scripto.backend.report.entity.DocumentReport;

import java.time.LocalDateTime;

public record AdminReportDTO(
        Long id,
        Long documentId,
        String documentTitle,
        Long ownerId,
        String ownerName,
        Long reporterId,
        String reporterName,
        ReportReason reason,
        String details,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
    public static AdminReportDTO from(DocumentReport report) {
        var reporter = report.getReporter();
        return new AdminReportDTO(
                report.getId(),
                report.getDocument().getId(),
                report.getDocument().getTitle(),
                report.getDocument().getUser().getId(),
                report.getDocument().getUser().getFullName(),
                reporter == null ? null : reporter.getId(),
                reporter == null ? "Usuário removido" : reporter.getFullName(),
                report.getReason(),
                report.getDetails(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt()
        );
    }
}
