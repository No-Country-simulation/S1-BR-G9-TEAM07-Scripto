package com.scripto.backend.summary.service;

import com.scripto.backend.summary.repository.DocumentSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryLookupService {
    private final DocumentSummaryRepository summaryRepository;

    public SummaryLookupService(DocumentSummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @Transactional(readOnly = true)
    public String findText(Long documentId) {
        return summaryRepository.findTextByDocumentId(documentId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> findTexts(Collection<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return summaryRepository.findTextsByDocumentIds(documentIds).stream()
                .collect(Collectors.toMap(
                        DocumentSummaryRepository.SummaryTextProjection::getDocumentId,
                        DocumentSummaryRepository.SummaryTextProjection::getSummary
                ));
    }
}
