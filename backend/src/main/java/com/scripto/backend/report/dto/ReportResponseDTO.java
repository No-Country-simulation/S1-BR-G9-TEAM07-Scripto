package com.scripto.backend.report.dto;

import com.scripto.backend.report.domain.ReportReason;
import com.scripto.backend.report.domain.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponseDTO(
        Long id,
        Long documentId,
        Long reporterId,
        ReportReason reason,
        String details,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
}
