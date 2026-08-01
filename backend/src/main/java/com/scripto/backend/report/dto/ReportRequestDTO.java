package com.scripto.backend.report.dto;

import com.scripto.backend.report.domain.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para denunciar um documento")
public record ReportRequestDTO(
        @Schema(description = "Motivo da denúncia", example = "HATEFUL_CONTENT")
        @NotNull ReportReason reason,

        @Schema(description = "Detalhes adicionais da denúncia", example = "O documento contém conteúdo ofensivo.", maxLength = 500)
        @Size(max = 500) String details
) {
}
