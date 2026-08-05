package com.scripto.backend.summary.dto;

import java.time.LocalDateTime;

public record SummaryResponseDTO(
        Long documentId,
        String summary,
        String provider,
        String model,
        boolean cached,
        int remainingGenerationsToday,
        LocalDateTime createdAt
) {
}
