package com.scripto.backend.user.dto;

import com.scripto.backend.aianalyse.domain.Level;

public record UserProfileStatsDTO(
        long totalProcessedDocuments,
        String mostFrequentCategory,
        Level mostFrequentLevel
) {
}
