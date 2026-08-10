package com.scripto.backend.recommendation.dto;

import com.scripto.backend.aianalyse.domain.Level;

import java.time.LocalDateTime;
import java.util.List;

public record RecommendationDTO(
        Long documentId,
        String title,
        String authorName,
        String category,
        Level difficulty,
        List<String> tags,
        String summary,
        LocalDateTime createdAt,
        double score,
        double semanticSimilarity
) {
}
