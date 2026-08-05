package com.scripto.backend.classification.domain;

import com.scripto.backend.aianalyse.domain.Level;

import java.util.List;

public record LocalClassificationResult(
        String category,
        double categoryConfidence,
        Level difficulty,
        double difficultyConfidence,
        List<TagPrediction> tagPredictions,
        double centroidSimilarity,
        List<FallbackReason> fallbackReasons,
        String modelVersion,
        float[] embedding
) {
    public boolean accepted() {
        return fallbackReasons == null || fallbackReasons.isEmpty();
    }

    public List<String> tags() {
        return tagPredictions == null
                ? List.of()
                : tagPredictions.stream().map(TagPrediction::name).toList();
    }
}
