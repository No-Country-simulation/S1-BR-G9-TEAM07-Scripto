package com.scripto.backend.classification.nemotron;

import com.scripto.backend.aianalyse.domain.Level;

import java.util.List;

public record NemotronClassification(
        String category,
        Level difficulty,
        List<String> tags,
        String suggestedCategory
) {
}
