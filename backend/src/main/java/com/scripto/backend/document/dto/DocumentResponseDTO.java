package com.scripto.backend.document.dto;

import com.scripto.backend.aianalyse.dto.AnalysisResultDTO;
import com.scripto.backend.document.domain.Status;

import java.time.LocalDateTime;

public record DocumentResponseDTO(

        Long documentId,

        String title,

        String content,

        Status status,

        AnalysisResultDTO analyses,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
