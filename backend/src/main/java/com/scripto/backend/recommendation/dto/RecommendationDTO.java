package com.scripto.backend.recommendation.dto;

import java.util.List;

public record RecommendationDTO(
        Long documentId,
        String title,
        String category,
        List<String> tags,
        double score,
        double semanticSimilarity
) {
}
