package com.scripto.backend.document.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.aianalyse.entity.AIAnalyse;
import com.scripto.backend.aianalyse.repository.AIAnalysisRepository;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.error.entity.DocumentProcessingError;
import com.scripto.backend.document.error.repository.DocumentProcessingErrorRepository;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.classification.exception.ClassificationUnavailableException;
import com.scripto.backend.exception.AiRetentionUnavailableException;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.tag.service.TagService;
import com.scripto.backend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DocumentPersistenceService {
    private static final String CURRENT_TRAINING_TERMS_VERSION = "2026-08-08";
    private static final String CURRENT_USAGE_TERMS_VERSION = "2026-08-08";
    private final DocumentRepository documentRepository;
    private final AIAnalysisRepository analysisRepository;
    private final TagService tagService;
    private final ObjectMapper objectMapper;
    private final DocumentProcessingErrorRepository processingErrorRepository;

    public DocumentPersistenceService(
            DocumentRepository documentRepository,
            AIAnalysisRepository analysisRepository,
            TagService tagService,
            ObjectMapper objectMapper,
            DocumentProcessingErrorRepository processingErrorRepository
    ) {
        this.documentRepository = documentRepository;
        this.analysisRepository = analysisRepository;
        this.tagService = tagService;
        this.objectMapper = objectMapper;
        this.processingErrorRepository = processingErrorRepository;
    }

    @Transactional
    public Document createPending(DocumentRequestDTO request, User user) {
        Document document = new Document(request.title().strip(), request.content().strip());
        document.setUser(user);
        document.setStatus(Status.PENDING);
        document.setVisibility(request.resolvedVisibility());
        document.setModerationStatus(ModerationStatus.APPROVED);
        document.setExternalAiAllowed(request.allowsExternalAi());
        document.setTrainingUseAllowed(request.allowsTrainingUse());
        if (request.allowsTrainingUse()) {
            document.setTrainingUseAcceptedAt(java.time.LocalDateTime.now());
            document.setTrainingTermsVersion(CURRENT_TRAINING_TERMS_VERSION);
        }
        if (request.acceptsUsageTerms()) {
            document.setUsageTermsAcceptedAt(java.time.LocalDateTime.now());
            document.setUsageTermsVersion(CURRENT_USAGE_TERMS_VERSION);
        }
        return documentRepository.saveAndFlush(document);
    }

    @Transactional
    public Document markProcessing(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow();
        document.setStatus(Status.PROCESSING);
        return documentRepository.saveAndFlush(document);
    }

    @Transactional
    public Document complete(Long documentId, FinalClassification result) {
        Document document = documentRepository.findById(documentId).orElseThrow();
        AIAnalyse analysis = analysisRepository.findByDocument(document).orElseGet(AIAnalyse::new);
        analysis.setDocument(document);
        analysis.setCategory(result.category());
        analysis.setDifficulty(result.difficulty());
        analysis.setCategoryConfidence(result.categoryConfidence());
        analysis.setDifficultyConfidence(result.difficultyConfidence());
        analysis.setSource(result.source());
        analysis.setModelVersion(result.modelVersion());
        analysis.setExternalModel(result.externalModel());
        analysis.setSuggestedCategory(result.suggestedCategory());
        try {
            analysis.setFallbackReasons(objectMapper.writeValueAsString(result.fallbackReasons()));
            analysis.setOriginalJson(objectMapper.writeValueAsString(toAuditValue(result)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize classification result", exception);
        }
        analysisRepository.save(analysis);
        tagService.attachTags(document, result.tags());
        document.setStatus(Status.PROCESSED);
        document.setAiAnalyse(analysis);
        document = documentRepository.saveAndFlush(document);
        return document;
    }

    private Map<String, Object> toAuditValue(FinalClassification result) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("category", result.category());
        value.put("categoryConfidence", result.categoryConfidence());
        value.put("difficulty", result.difficulty() == null ? null : result.difficulty().name());
        value.put("difficultyConfidence", result.difficultyConfidence());
        value.put("tags", result.tags());
        value.put("source", result.source() == null ? null : result.source().name());
        value.put("modelVersion", result.modelVersion());
        value.put("externalModel", result.externalModel());
        value.put("fallbackReasons", result.fallbackReasons());
        value.put("suggestedCategory", result.suggestedCategory());
        return value;
    }

    @Transactional
    public void discardFailed(Long documentId, RuntimeException exception) {
        documentRepository.findById(documentId).ifPresent(documentRepository::delete);
        processingErrorRepository.save(new DocumentProcessingError(toSafeFailureReason(exception)));
    }

    private String toSafeFailureReason(RuntimeException exception) {
        if (exception instanceof ClassificationUnavailableException) {
            return "CLASSIFICATION_UNAVAILABLE";
        }
        if (exception instanceof AiRetentionUnavailableException) {
            return "AI_RETENTION_UNAVAILABLE";
        }
        return exception.getClass().getSimpleName().replaceAll("[^A-Za-z0-9_]", "_").toUpperCase();
    }
}
