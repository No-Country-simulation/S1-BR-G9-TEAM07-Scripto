package com.scripto.backend.classification.service;

import com.scripto.backend.classification.config.ScriptoAiProperties;
import com.scripto.backend.classification.domain.ClassificationInput;
import com.scripto.backend.classification.domain.ClassificationSource;
import com.scripto.backend.classification.domain.FallbackReason;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.classification.domain.LocalClassificationResult;
import com.scripto.backend.classification.exception.ClassificationUnavailableException;
import com.scripto.backend.classification.local.LocalClassificationEngine;
import com.scripto.backend.classification.nemotron.NemotronClassification;
import com.scripto.backend.classification.nemotron.NemotronClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClassificationOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(ClassificationOrchestrator.class);

    private final LocalClassificationEngine localEngine;
    private final NemotronClient nemotronClient;
    private final ClassificationResultValidator validator;
    private final ScriptoAiProperties aiProperties;

    public ClassificationOrchestrator(
            @Lazy LocalClassificationEngine localEngine,
            NemotronClient nemotronClient,
            ClassificationResultValidator validator,
            ScriptoAiProperties aiProperties
    ) {
        this.localEngine = localEngine;
        this.nemotronClient = nemotronClient;
        this.validator = validator;
        this.aiProperties = aiProperties;
    }

    public FinalClassification classify(ClassificationInput input) {
        LocalClassificationResult localAttempt = null;
        List<FallbackReason> reasons = new ArrayList<>();
        if (!aiProperties.isLocalEnabled()) {
            reasons.add(FallbackReason.LOCAL_MODEL_DISABLED);
        } else {
            try {
                localAttempt = localEngine.classify(input.title(), input.content());
                reasons.addAll(localAttempt.fallbackReasons());
                if (localAttempt.accepted()) {
                    return validator.validate(new FinalClassification(
                            localAttempt.category(),
                            localAttempt.categoryConfidence(),
                            localAttempt.difficulty(),
                            localAttempt.difficultyConfidence(),
                            localAttempt.tags(),
                            ClassificationSource.LOCAL,
                            localAttempt.modelVersion(),
                            null,
                            List.of(),
                            null,
                            localAttempt.embedding(),
                            localAttempt
                    ));
                }
                log.info("Local classification routed to fallback: {}", reasons);
            } catch (RuntimeException exception) {
                reasons.add(FallbackReason.LOCAL_MODEL_ERROR);
                log.warn("Local classification failed and will use fallback: {}", exception.getMessage());
            }
        }

        if (!input.externalAiAllowed()) {
            reasons.add(FallbackReason.EXTERNAL_AI_NOT_ALLOWED);
            throw new ClassificationUnavailableException(
                    "Local classification was not accepted and external AI processing is disabled"
            );
        }

        NemotronClassification external = nemotronClient.classify(input.title(), input.content());
        return validator.validate(new FinalClassification(
                external.category(),
                null,
                external.difficulty(),
                null,
                external.tags(),
                ClassificationSource.NEMOTRON,
                localAttempt == null ? null : localAttempt.modelVersion(),
                nemotronClient.modelName(),
                List.copyOf(reasons),
                external.suggestedCategory(),
                localAttempt == null ? null : localAttempt.embedding(),
                localAttempt
        ));
    }
}
