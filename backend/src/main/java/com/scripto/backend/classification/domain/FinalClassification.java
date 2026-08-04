package com.scripto.backend.classification.domain;

import com.scripto.backend.aianalyse.domain.Level;

import java.util.List;

public record FinalClassification(
        String category,
        Double categoryConfidence,
        Level difficulty,
        Double difficultyConfidence,
        List<String> tags,
        ClassificationSource source,
        String modelVersion,
        String externalModel,
        List<FallbackReason> fallbackReasons,
        String suggestedCategory,
        float[] embedding,
        LocalClassificationResult localAttempt
) {
}
