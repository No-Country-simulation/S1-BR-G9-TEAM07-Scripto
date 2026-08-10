package com.scripto.backend.document.dto;

import com.scripto.backend.aianalyse.dto.AIAnalysisResultDTO;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;

import java.time.LocalDateTime;

public record DocumentResponseDTO(
        Long documentId,
        String title,
        String content,
        Status status,
        Visibility visibility,
        ModerationStatus moderationStatus,
        boolean externalAiAllowed,
        boolean trainingUseAllowed,
        AIAnalysisResultDTO analysis,
        String summary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
