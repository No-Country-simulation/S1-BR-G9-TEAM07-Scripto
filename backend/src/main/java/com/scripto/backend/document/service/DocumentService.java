package com.scripto.backend.document.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.aianalyse.dto.AIAnalysisResultDTO;
import com.scripto.backend.aianalyse.entity.AIAnalyse;
import com.scripto.backend.classification.domain.ClassificationInput;
import com.scripto.backend.classification.domain.FallbackReason;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.classification.service.ClassificationOrchestrator;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.dto.DocumentListDTO;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.dto.DocumentResponseDTO;
import com.scripto.backend.document.dto.PublicDocumentDTO;
import com.scripto.backend.document.dto.VisibilityUpdateDTO;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.exception.ResourceNotFoundException;
import com.scripto.backend.summary.service.SummaryLookupService;
import com.scripto.backend.tag.service.TagService;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.vector.VectorStore;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentPersistenceService persistenceService;
    private final ClassificationOrchestrator classificationOrchestrator;
    private final VectorStore vectorStore;
    private final TagService tagService;
    private final SummaryLookupService summaryLookupService;
    private final ObjectMapper objectMapper;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentPersistenceService persistenceService,
            ClassificationOrchestrator classificationOrchestrator,
            VectorStore vectorStore,
            TagService tagService,
            SummaryLookupService summaryLookupService,
            ObjectMapper objectMapper
    ) {
        this.documentRepository = documentRepository;
        this.persistenceService = persistenceService;
        this.classificationOrchestrator = classificationOrchestrator;
        this.vectorStore = vectorStore;
        this.tagService = tagService;
        this.summaryLookupService = summaryLookupService;
        this.objectMapper = objectMapper;
    }

    public DocumentResponseDTO sendDocument(@Valid DocumentRequestDTO request, User user) {
        if (!request.allowsTrainingUse()) {
            throw new BusinessRuleException("É necessário aceitar o uso do documento para o modelo interno antes da análise.");
        }
        if (!request.acceptsUsageTerms()) {
            throw new BusinessRuleException("É necessário aceitar os Termos e a Política de Privacidade antes do envio.");
        }

        Document document = persistenceService.createPending(request, user);
        persistenceService.markProcessing(document.getId());
        try {
            FinalClassification classification = classificationOrchestrator.classify(new ClassificationInput(
                    document.getTitle(),
                    document.getContent(),
                    document.isExternalAiAllowed()
            ));
            persistenceService.complete(document.getId(), classification);
            vectorStore.recordClassification(
                    document.getId(),
                    document.getTitle(),
                    document.getContent(),
                    document.getVisibility(),
                    document.isTrainingUseAllowed(),
                    classification
            );
            return toResponse(loadDetailedOwned(document.getId(), user));
        } catch (RuntimeException exception) {
            persistenceService.discardFailed(document.getId(), exception);
            throw exception;
        }
    }

    public DocumentResponseDTO findById(Long documentId, User user) {
        return toResponse(loadDetailedOwned(documentId, user));
    }

    public PublicDocumentDTO findPublicById(Long documentId) {
        Document document = documentRepository
                .findByIdAndVisibilityAndModerationStatusAndStatus(
                        documentId, Visibility.PUBLIC, ModerationStatus.APPROVED, Status.PROCESSED
                )
                .orElseThrow(() -> new ResourceNotFoundException("Documento público não encontrado."));
        return new PublicDocumentDTO(document, summaryLookupService.findText(document.getId()));
    }

    public List<DocumentListDTO> findDocuments(User user, String category, String tag, Level level, Status status) {
        if (status == Status.ERROR) {
            return List.of();
        }
        String normalizedTag = tag == null ? null : tagService.normalizeKey(tag);
        List<Document> documents = documentRepository.findByFilters(user, category, normalizedTag, level, status);
        Map<Long, String> summaries = summaryLookupService.findTexts(documents.stream().map(Document::getId).toList());
        return documents.stream()
                .map(document -> new DocumentListDTO(document, summaries.get(document.getId())))
                .toList();
    }

    public DocumentResponseDTO updateVisibility(Long documentId, VisibilityUpdateDTO dto, User user) {
        Document document = documentRepository.findDetailedById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado."));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Documento não encontrado.");
        }


        if (document.getModerationStatus() == ModerationStatus.BLOCKED) {
            if (dto.visibility() == Visibility.PUBLIC) {
                throw new BusinessRuleException("Documentos bloqueados pela moderação não podem se tornar públicos.");
            }
        }

        document.setVisibility(dto.visibility());
        documentRepository.save(document);
        return toResponse(document);
    }

    public List<PublicDocumentDTO> listPublicDocuments(User currentUser, String category, String tag, Level difficulty, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(safePage, safeSize);
        String normalizedTag = tag == null ? null : tagService.normalizeKey(tag);
        List<Long> ids = documentRepository.findPublicDocumentIds(
                category, normalizedTag, difficulty, currentUser, pageable
        );
        if (ids.isEmpty()) {
            return List.of();
        }
        var order = new java.util.HashMap<Long, Integer>();
        for (int index = 0; index < ids.size(); index++) {
            order.put(ids.get(index), index);
        }
        List<Document> documents = documentRepository.findPublicDetailedByIdsExcludingUser(ids, currentUser).stream()
                .sorted(java.util.Comparator.comparingInt(document -> order.getOrDefault(document.getId(), Integer.MAX_VALUE)))
                .toList();
        Map<Long, String> summaries = summaryLookupService.findTexts(documents.stream().map(Document::getId).toList());
        return documents.stream()
                .map(document -> new PublicDocumentDTO(document, summaries.get(document.getId())))
                .toList();
    }

    public Document loadDetailedOwned(Long documentId, User user) {
        Document document = documentRepository.findDetailedById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado."));
        if (!document.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Documento não encontrado.");
        }
        return document;
    }

    private DocumentResponseDTO toResponse(Document document) {
        AIAnalyse analysis = document.getAiAnalyse();
        AIAnalysisResultDTO analysisDto = null;
        if (analysis != null) {
            analysisDto = new AIAnalysisResultDTO(
                    analysis.getId(),
                    analysis.getCategory(),
                    analysis.getCategoryConfidence(),
                    document.getDocumentTags().stream().map(item -> item.getTag().getName()).toList(),
                    analysis.getDifficulty(),
                    analysis.getDifficultyConfidence(),
                    analysis.getSource(),
                    analysis.getModelVersion(),
                    analysis.getExternalModel(),
                    parseFallbackReasons(analysis.getFallbackReasons()),
                    analysis.getSuggestedCategory(),
                    analysis.getCreatedAt()
            );
        }
        return new DocumentResponseDTO(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getStatus(),
                document.getVisibility(),
                document.getModerationStatus(),
                document.isExternalAiAllowed(),
                document.isTrainingUseAllowed(),
                analysisDto,
                summaryLookupService.findText(document.getId()),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private List<FallbackReason> parseFallbackReasons(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception exception) {
            return List.of();
        }
    }

    @Transactional
    public void deleteDocument(Long documentId, User user) {
        Document document = documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado."));
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public Page<DocumentListDTO> findAllDocuments(Pageable pageable) {
        Page<Document> documents = documentRepository.findAll(pageable);
        Map<Long, String> summaries = summaryLookupService.findTexts(documents.getContent().stream().map(Document::getId).toList());
        return documents.map(document -> new DocumentListDTO(document, summaries.get(document.getId())));
    }
}