package com.scripto.backend.report.dto;

import com.scripto.backend.report.domain.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Decisão administrativa sobre uma denúncia")
public record ReportReviewDTO(
        @Schema(description = "Novo status da denúncia", example = "ACTIONED")
        @NotNull ReportStatus status,

        @Schema(description = "Indica se o documento deve ser bloqueado", example = "true")
        boolean blockDocument
) {
}
