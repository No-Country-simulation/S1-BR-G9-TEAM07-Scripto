package com.scripto.backend.summary.service;

import com.scripto.backend.classification.nemotron.NemotronClient;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.service.DocumentService;
import com.scripto.backend.summary.dto.SummaryResponseDTO;
import com.scripto.backend.summary.entity.DocumentSummary;
import com.scripto.backend.summary.repository.DocumentSummaryRepository;
import com.scripto.backend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SummaryService {
    private final DocumentService documentService;
    private final DocumentSummaryRepository summaryRepository;
    private final DailySummaryQuotaService quotaService;
    private final NemotronClient nemotronClient;

    public SummaryService(
            DocumentService documentService,
            DocumentSummaryRepository summaryRepository,
            DailySummaryQuotaService quotaService,
            NemotronClient nemotronClient
    ) {
        this.documentService = documentService;
        this.summaryRepository = summaryRepository;
        this.quotaService = quotaService;
        this.nemotronClient = nemotronClient;
    }

    @Transactional
    public SummaryResponseDTO summarize(Long documentId, User user) {
        Document document = documentService.loadDetailedOwned(documentId, user);
        return summaryRepository.findByDocument(document)
                .map(summary -> toResponse(summary, true, quotaService.remaining(user)))
                .orElseGet(() -> createSummary(document, user));
    }

    private SummaryResponseDTO createSummary(Document document, User user) {
        if (!document.isExternalAiAllowed()) {
            throw new IllegalArgumentException("O processamento por IA externa está desabilitado para este documento.");
        }
        int remaining = quotaService.consume(user);
        String generated = nemotronClient.summarize(document.getTitle(), document.getContent());
        DocumentSummary summary = new DocumentSummary();
        summary.setDocument(document);
        summary.setSummary(generated);
        summary.setProvider("NEMOTRON");
        summary.setModel(nemotronClient.modelName());
        summary = summaryRepository.saveAndFlush(summary);
        return toResponse(summary, false, remaining);
    }

    private SummaryResponseDTO toResponse(DocumentSummary summary, boolean cached, int remaining) {
        return new SummaryResponseDTO(
                summary.getDocument().getId(),
                summary.getSummary(),
                summary.getProvider(),
                summary.getModel(),
                cached,
                remaining,
                summary.getCreatedAt()
        );
    }
}