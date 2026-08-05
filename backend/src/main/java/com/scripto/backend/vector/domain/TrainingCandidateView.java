package com.scripto.backend.vector.domain;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record TrainingCandidateView(
        Long id,
        Long documentId,
        String contentHash,
        String title,
        String content,
        JsonNode localResult,
        JsonNode nemotronResult,
        String status,
        Long reviewedByUserId,
        OffsetDateTime createdAt,
        OffsetDateTime reviewedAt
) {
}
