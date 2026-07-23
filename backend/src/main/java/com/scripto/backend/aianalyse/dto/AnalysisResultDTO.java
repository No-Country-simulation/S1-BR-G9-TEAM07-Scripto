package com.scripto.backend.aianalyse.dto;

import com.scripto.backend.aianalyse.domain.Level;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResultDTO(

        Long analysisId,

        String category,

        Double probability,

        List<String> tags,

        Level knowledgeLevel,

        String summary,

        LocalDateTime createdAt
) {
}
