package com.scripto.backend.classification.service;

import com.scripto.backend.classification.domain.ClassificationSource;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.classification.exception.ClassificationUnavailableException;
import com.scripto.backend.classification.local.RuntimeBundleLoader;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Component
public class ClassificationResultValidator {
    private final RuntimeBundleLoader loader;

    public ClassificationResultValidator(RuntimeBundleLoader loader) {
        this.loader = loader;
    }

    public FinalClassification validate(FinalClassification result) {
        if (result == null) {
            throw new ClassificationUnavailableException("Classification result is null");
        }
        if (!loader.bundle().taxonomyCategories().contains(result.category())) {
            throw new ClassificationUnavailableException("Classification category is outside the taxonomy");
        }
        if (result.difficulty() == null || result.source() == null) {
            throw new ClassificationUnavailableException("Classification difficulty or source is missing");
        }
        validateConfidence(result.categoryConfidence(), "category");
        validateConfidence(result.difficultyConfidence(), "difficulty");

        LinkedHashSet<String> normalizedTags = new LinkedHashSet<>();
        if (result.tags() != null) {
            for (String rawTag : result.tags()) {
                String tag = rawTag == null
                        ? ""
                        : rawTag.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
                if (!tag.isBlank() && tag.length() <= 50) {
                    normalizedTags.add(tag);
                }
            }
        }
        List<String> tags = List.copyOf(normalizedTags);
        if (tags.isEmpty() || tags.size() > 5) {
            throw new ClassificationUnavailableException("Classification must contain between one and five valid tags");
        }

        String suggestedCategory = result.suggestedCategory() == null
                ? null
                : result.suggestedCategory().strip();
        if ("Other".equals(result.category())) {
            if (result.source() == ClassificationSource.LOCAL) {
                throw new ClassificationUnavailableException("Other cannot be accepted from the local model");
            }
            if (suggestedCategory == null
                    || suggestedCategory.isBlank()
                    || suggestedCategory.length() > 120
                    || "Other".equalsIgnoreCase(suggestedCategory)) {
                throw new ClassificationUnavailableException("Other requires a valid suggested category");
            }
        } else {
            suggestedCategory = null;
        }

        return new FinalClassification(
                result.category(),
                result.categoryConfidence(),
                result.difficulty(),
                result.difficultyConfidence(),
                tags,
                result.source(),
                result.modelVersion(),
                result.externalModel(),
                result.fallbackReasons() == null ? List.of() : List.copyOf(result.fallbackReasons()),
                suggestedCategory,
                result.embedding(),
                result.localAttempt()
        );
    }

    private void validateConfidence(Double confidence, String field) {
        if (confidence != null && (!Double.isFinite(confidence) || confidence < 0.0d || confidence > 1.0d)) {
            throw new ClassificationUnavailableException("Invalid " + field + " confidence");
        }
    }
}
