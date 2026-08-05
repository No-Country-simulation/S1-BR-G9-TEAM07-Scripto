package com.scripto.backend.aianalyse.dto;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.classification.domain.ClassificationSource;
import com.scripto.backend.classification.domain.FallbackReason;

import java.time.LocalDateTime;
import java.util.List;

public record AIAnalysisResultDTO(
        Long analysisId,
        String category,
        Double categoryConfidence,
        List<String> tags,
        Level difficulty,
        Double difficultyConfidence,
        ClassificationSource source,
        String modelVersion,
        String externalModel,
        List<FallbackReason> fallbackReasons,
        String suggestedCategory,
        LocalDateTime createdAt
) {
}
